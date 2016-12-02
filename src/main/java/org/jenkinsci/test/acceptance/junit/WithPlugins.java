package org.jenkinsci.test.acceptance.junit;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

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

import hudson.util.VersionNumber;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

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

        @Override
        public Statement apply(final Statement base, final Description d) {
            return new Statement() {
                private Jenkins jenkins;
                @Override
                public void evaluate() throws Throwable {
                    jenkins = injector.getInstance(Jenkins.class);
                    List<PluginSpec> plugins = combinePlugins(
                            d.getAnnotation(WithPlugins.class),
                            d.getTestClass().getAnnotation(WithPlugins.class)
                    );

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
                    System.out.println("... End of setup for " + getDescription(d));
                    System.out.println("=== Starting test " + getDescription(d));
                    base.evaluate();
                    System.out.println("=== End of test " + getDescription(d));
                }

                private List<PluginSpec> combinePlugins(WithPlugins... wp) {
                    ArrayList<PluginSpec> plugins = new ArrayList<>();
                    for (WithPlugins withPlugins : wp) {
                        if (withPlugins != null) {
                            for (String spec: withPlugins.value()) {
                                // TODO eliminate duplicates and prefer newer versions
                                plugins.add(new PluginSpec(spec));
                            }
                        }
                    }

                    return plugins;
                }

                private void installPlugins(List<PluginSpec> install) {
                    PluginManager pm = jenkins.getPluginManager();

                    for (Iterator<PluginSpec> iterator = install.iterator(); iterator.hasNext(); ) {
                        PluginSpec spec = iterator.next();
                        switch (pm.installationStatus(spec)) {
                            case NOT_INSTALLED:
                                LOGGER.info(spec + " is up to date");
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

        private String getDescription(final Description d) {
            return d + ": " + new SimpleDateFormat("HH:mm:ss").format(new Date());
        }
    }
}
