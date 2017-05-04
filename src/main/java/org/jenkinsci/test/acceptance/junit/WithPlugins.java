package org.jenkinsci.test.acceptance.junit;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.google.common.annotations.VisibleForTesting;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.jenkinsci.test.acceptance.po.Plugin;
import org.jenkinsci.test.acceptance.po.PluginManager;
import org.jenkinsci.test.acceptance.update_center.PluginSpec;
import org.jenkinsci.test.acceptance.update_center.UpdateCenterMetadata.UnableToResolveDependencies;
import org.jenkinsci.test.acceptance.utils.pluginreporter.ExercisedPluginsReporter;
import org.junit.internal.AssumptionViolatedException;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import hudson.util.VersionNumber;

/**
 * Indicates that a test requires the presence of the specified plugins.
 *
 * Example: <tt>@WithPlugin("subversion")</tt>
 * <p/>
 * One can specify a specific minimum version after the plugin name with a suffixed '@'.
 * <p/>
 * Example: <tt>@WithPlugin("subversion@1.54")</tt>
 *
 * The latter example declares that running the test with older version is pointless, typically because of missing feature.
 *
 * The annotation guarantees that the plugin is installed in required or later version.
 * If the plugin is already installed but not in correct version then
 * the environment variable NEVER_REPLACE_EXISTING_PLUGINS is evaluated:
 * <ul>
 *     <li>if the environment variable is set then the test will be skipped.</li>
 *     <li>if the environment variable is undefined then the installed version
 *     of the plugin is overwritten with the latest version of the plugin. If
 *     required version is not available in update center, the test will fail.</li>
*  </ul>
 *
 * @author Kohsuke Kawaguchi
 */
@Retention(RUNTIME)
@Target({METHOD, TYPE})
@Inherited
@Documented
@RuleAnnotation(value=WithPlugins.RuleImpl.class, priority=WithPlugins.PRIORITY)
public @interface WithPlugins {

    int PRIORITY = 10;

    /**
     * See {@link PluginSpec} for the syntax.
     */
    String[] value();

    class RuleImpl implements TestRule {

        private static final Logger LOGGER = Logger.getLogger(WithPlugins.class.getName());

        @Inject
        Injector injector;

        @Inject @Named("ExercisedPluginReporter")
        ExercisedPluginsReporter pluginReporter;

        @Inject(optional=true) @Named("neverReplaceExistingPlugins")
        boolean neverReplaceExistingPlugins;

        @VisibleForTesting static List<PluginSpec> combinePlugins(List<WithPlugins> wp) {
            Map<String, PluginSpec> plugins = new LinkedHashMap<>();
            for (WithPlugins withPlugins : wp) {
                if (withPlugins != null) {
                    // eliminate duplicates and prefer newer versions
                    for (String spec: withPlugins.value()) {
                        PluginSpec candidate = new PluginSpec(spec);
                        PluginSpec existing = plugins.get(candidate.getName());

                        if (existing == null || existing.getVersion() == null) {
                            // first declaration or override of unversioned declaration
                            plugins.put(candidate.getName(), candidate);
                        } else if (candidate.getVersion() == null) {
                            // Existing is equally or more specific - noop
                        } else if (candidate.getVersionNumber().isNewerThan(existing.getVersionNumber())) {
                            // Candidate requires never version - replace
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
                    installPlugins(plugins);

                    for (PluginSpec plugin : plugins) {
                        Plugin installedPlugin = jenkins.getPlugin(plugin.getName());
                        VersionNumber installedVersion = installedPlugin.getVersion();
                        String version = installedVersion.toString();
                        pluginReporter.log(
                                d.getClassName() + "." + d.getMethodName(),
                                plugin.getName(),
                                version
                        );
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
                                if (neverReplaceExistingPlugins) {
                                    throw new AssumptionViolatedException(String.format(
                                            "Test requires %s plugin", spec
                                    ));
                                }
                                break;
                            default:
                                assert false;
                        }
                    }

                    if (install.isEmpty()) {
                        LOGGER.info("All required plugins already installed.");
                    }
                    else {
                        LOGGER.info("Installing plugins for test: " + install);
                        PluginSpec[] installList = install.toArray(new PluginSpec[install.size()]);
                        try {
                            //noinspection deprecation
                            pm.installPlugins(installList);
                        } catch (UnableToResolveDependencies ex) {
                            throw new AssumptionViolatedException("Unable to install required plugins", ex);
                        }
                    }
                }
            };
        }
    }
}
