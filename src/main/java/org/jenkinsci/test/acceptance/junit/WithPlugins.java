package org.jenkinsci.test.acceptance.junit;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;
import hudson.util.VersionNumber;
import java.io.IOException;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.jenkinsci.test.acceptance.po.Plugin;
import org.jenkinsci.test.acceptance.po.PluginManager;
import org.jenkinsci.test.acceptance.update_center.PluginSpec;
import org.jenkinsci.test.acceptance.update_center.UpdateCenterMetadata.UnableToResolveDependencies;
import org.jenkinsci.test.acceptance.utils.pluginreporter.ExercisedPluginsReporter;
import org.junit.AssumptionViolatedException;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Indicates that a test requires the presence of the specified plugins.
 * <p>
 * Example: {@code @WithPlugin("subversion")}
 * <p>
 * One can specify a specific minimum version after the plugin name with a suffixed '@'.
 * <p>
 * Example: {@code @WithPlugin("subversion@1.54")}
 * <p>
 * The latter example declares that running the test with older version is pointless, typically because of missing feature.
 * <p>
 * In normal mode the annotation guarantees that the plugin is installed in required or later version.
 * <p>
 * There is also a pre configured plugins mode, running in this mode means that the ATH is using a war file that (somehow)
 * has already preconfigured all the plugins that are to be tested, in that case the ATH only validates that the
 * pre configured plugin universe is enough to run the tests and don't try to modify the existing plugins in any way.
 * <p>
 * If the existing plugin configuration is not enough to run the test the end result depends on the configured value
 * for the configuration property pluginEvaluationOutcome
 * <ul>
 *     <li>failOnInvalid means the test is to fail if the plugin configuration is not valid for the test</li>
 *     <li>skipOnInvalid means the test is to be skipped if the plugin configuration is not valid for the test</li>
 * </ul>
 *
 * Pre configured plugins mode is activated if and only if pluginEvaluationOutcome has a not null value, by default is
 * not active
 *
 * @author Kohsuke Kawaguchi
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Inherited
@Documented
@RuleAnnotation(value = WithPlugins.RuleImpl.class, priority = WithPlugins.PRIORITY)
public @interface WithPlugins {

    int PRIORITY = 10;

    /**
     * See {@link PluginSpec} for the syntax.
     */
    String[] value();

    class RuleImpl implements TestRule {

        private static final Logger LOGGER = Logger.getLogger(WithPlugins.class.getName());
        private static final String FAIL_ON_INVALID = "failOnInvalid";
        private static final String SKIP_ON_INVALID = "skipOnInvalid";
        private static final String PRECONFIGURED_MODE_DISABLED = "validationDisabled";

        @Inject
        Injector injector;

        @Inject
        @Named("ExercisedPluginReporter")
        ExercisedPluginsReporter pluginReporter;

        String pluginEvaluationOutcome = System.getProperty("pluginEvaluationOutcome", PRECONFIGURED_MODE_DISABLED);

        @VisibleForTesting
        static List<PluginSpec> combinePlugins(List<WithPlugins> wp) {
            Map<String, PluginSpec> plugins = new LinkedHashMap<>();
            for (WithPlugins withPlugins : wp) {
                if (withPlugins != null) {
                    // eliminate duplicates and prefer newer versions
                    for (String spec : withPlugins.value()) {
                        PluginSpec candidate = new PluginSpec(spec);
                        PluginSpec existing = plugins.get(candidate.getName());

                        if (existing == null || existing.getVersion() == null) {
                            // first declaration or override of unversioned declaration
                            plugins.put(candidate.getName(), candidate);
                        } else if (candidate.getVersion() == null) {
                            // Existing is equally or more specific - noop
                        } else if (candidate.getVersionNumber().isNewerThan(existing.getVersionNumber())) {
                            // Candidate requires newer version - replace
                            plugins.put(candidate.getName(), candidate);
                        }
                    }
                }
            }

            return new ArrayList<>(plugins.values());
        }

        @Override
        public Statement apply(final Statement base, final Description d) {
            return new Statement() {
                private Jenkins jenkins;

                @Override
                public void evaluate() throws Throwable {
                    jenkins = injector.getInstance(Jenkins.class);

                    List<WithPlugins> wp = new LinkedList<>();
                    wp.add(d.getAnnotation(WithPlugins.class));

                    Class<?> testClass = d.getTestClass();
                    while (testClass != null) {
                        wp.add(testClass.getAnnotation(WithPlugins.class));
                        testClass = testClass.getSuperclass();
                    }

                    List<PluginSpec> plugins = combinePlugins(wp);

                    // Check if we are in preconfigured plugins mode
                    if (pluginEvaluationOutcome.equals(PRECONFIGURED_MODE_DISABLED)) {

                        installPlugins(plugins);

                        for (PluginSpec plugin : plugins) {
                            Plugin installedPlugin = jenkins.getPlugin(plugin.getName());
                            VersionNumber installedVersion = installedPlugin.getVersion();
                            String version = installedVersion.toString();
                            pluginReporter.log(d.getClassName() + "." + d.getMethodName(), plugin.getName(), version);
                        }
                    } else { // In preconfigured plugins mode, ATH will just validate plugins
                        PluginManager pm = jenkins.getPluginManager();

                        for (PluginSpec spec : plugins) {
                            PluginManager.InstallationStatus status = pm.installationStatus(spec);
                            if (!PluginManager.InstallationStatus.UP_TO_DATE.equals(status)) {
                                handleInvalidState(pluginEvaluationOutcome, spec, d, status);
                            }
                        }
                    }
                    base.evaluate();
                }

                private void installPlugins(List<PluginSpec> install) {
                    PluginManager pm = jenkins.getPluginManager();

                    for (Iterator<PluginSpec> iterator = install.iterator(); iterator.hasNext(); ) {
                        PluginSpec spec = iterator.next();
                        switch (pm.installationStatus(spec)) {
                            case NOT_INSTALLED:
                                LOGGER.info(spec + " is not installed");
                                break;
                            case UP_TO_DATE:
                                iterator.remove(); // Already installed
                                break;
                            case OUTDATED:
                                LOGGER.info(spec + " is outdated");
                                break;
                            default:
                                assert false;
                        }
                    }

                    if (install.isEmpty()) {
                        LOGGER.info("All required plugins already installed.");
                    } else {
                        LOGGER.info("Installing plugins for test: " + install);
                        PluginSpec[] installList = install.toArray(new PluginSpec[install.size()]);
                        try {
                            //noinspection deprecation
                            pm.installPlugins(installList);
                        } catch (UnableToResolveDependencies | IOException ex) {
                            throw new AssumptionViolatedException("Unable to install required plugins", ex);
                        }
                    }
                }
            };
        }

        private void handleInvalidState(
                String pluginEvaluationOutcome,
                PluginSpec spec,
                Description d,
                PluginManager.InstallationStatus status) {
            String format = String.format(
                    "%s plugin is required by test %s but it is not installed in a valid version and ATH is running in preconfigured mode",
                    spec, d.getDisplayName());
            if (status.equals(PluginManager.InstallationStatus.OUTDATED)) {
                Jenkins jenkins = injector.getInstance(Jenkins.class);
                Plugin existingPlugin = jenkins.getPlugin(spec.getName());
                format = String.format(
                        "%s Existing installed version of plugin %s is %s",
                        format, spec.getName(), existingPlugin.getVersion());
            }

            if (pluginEvaluationOutcome.equals(FAIL_ON_INVALID)) {
                throw new AssertionError(format);
            } else if (pluginEvaluationOutcome.equals(SKIP_ON_INVALID)) {
                throw new AssumptionViolatedException(format);
            } else {
                assert false : "unrecognized pluginEvaluationOutcome=" + pluginEvaluationOutcome;
            }
        }
    }
}
