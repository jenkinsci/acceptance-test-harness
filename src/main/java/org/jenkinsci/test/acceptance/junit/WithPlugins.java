package org.jenkinsci.test.acceptance.junit;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;

import org.jenkinsci.test.acceptance.po.Jenkins;
import org.jenkinsci.test.acceptance.po.Plugin;
import org.jenkinsci.test.acceptance.po.PluginManager;
import org.jenkinsci.test.acceptance.po.PluginManager.PluginSpec;
import org.jenkinsci.test.acceptance.update_center.UpdateCenterMetadata.UnableToResolveDependencies;
import org.jenkinsci.test.acceptance.utils.pluginreporter.ExercisedPluginsReporter;
import org.junit.internal.AssumptionViolatedException;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import hudson.util.VersionNumber;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import static org.junit.Assume.assumeTrue;

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
     * See {@link PluginManager.PluginSpec} for the syntax.
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
                    List<String> plugins = combinePlugins(
                            d.getAnnotation(WithPlugins.class),
                            d.getTestClass().getAnnotation(WithPlugins.class)
                    );
                    boolean restartRequired = installPlugins(plugins);
                    LOGGER.info("for " + d + " asked to install " + plugins + "; restartRequired? " + restartRequired);
                    if (restartRequired) {
                        assumeTrue("This test requires a restartable Jenkins", jenkins.canRestart());
                        jenkins.restart();
                    }
                    for (String name : plugins) {
                        name = name.replaceAll("@.*", "");
                        Plugin installedPlugin = jenkins.getPlugin(name);
                        VersionNumber installedVersion = installedPlugin.getVersion();
                        String version = installedVersion.toString();
                        pluginReporter.log(d.getClassName() + "." + d.getMethodName(), name, version);
                    }
                    base.evaluate();
                }

                private List<String> combinePlugins(WithPlugins... wp) {
                    ArrayList<String> plugins = new ArrayList<>();
                    for (WithPlugins withPlugins : wp) {
                        if (withPlugins != null) {
                            Collections.addAll(plugins, withPlugins.value());
                        }
                    }

                    return plugins;
                }

                private boolean installPlugins(List<String> install) {
                    PluginManager pm = jenkins.getPluginManager();

                    for (Iterator<String> iterator = install.iterator(); iterator.hasNext(); ) {
                        String spec = iterator.next();
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

                    LOGGER.info("Installing plugins for test: " + install);
                    String[] installList = install.toArray(new String[install.size()]);
                    try {
                        //noinspection deprecation
                        return pm.installPlugins(installList);
                    } catch (UnableToResolveDependencies ex) {
                        throw new AssumptionViolatedException("Unable to install required plugins", ex);
                    }
                }
            };
        }
    }
}
