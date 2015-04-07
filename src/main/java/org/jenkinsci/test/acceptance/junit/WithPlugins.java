package org.jenkinsci.test.acceptance.junit;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import org.jenkinsci.test.acceptance.controller.JenkinsController;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.jenkinsci.test.acceptance.po.Plugin;
import org.jenkinsci.test.acceptance.po.PluginManager;
import org.jenkinsci.test.acceptance.po.PluginManager.PluginSpec;
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

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

/**
 * Indicates that a test requires the presence of the specified plugins.
 * <p/>
 * Example: @WithPlugin("subversion")
 * <p/>
 * One can specify a specific minimum version after the plugin name with a suffixed '@'.
 * <p/>
 * Example: @WithPlugin("subversion@1.54")
 * <p/>
 * The annotation guarantees that the plugin is installed in required or later version. If required version is not
 * available in update center, the test will fail. If the plugin is already installed but not in correct version then
 * the environment variable NEVER_REPLACE_EXISTING_PLUGINS is evaluated:
 * <ul>
 *     <li>if the environment variable is set then the test will be skipped.</li>
 *     <li>if the environment variable is undefined then the installed version of the plugin is overwritten with
 *     the latest version of the plugin.</li>
*  </ul>
 * <p/>
 * When running tests, this annotation triggers {@link JenkinsAcceptanceTestRule} to install all the plugins.
 * <p/>
 * We also want to use this to filter tests, especially for non-destructive tests.
 *
 * @author Kohsuke Kawaguchi
 */
@Retention(RUNTIME)
@Target({METHOD, TYPE})
@Inherited
@Documented
@RuleAnnotation(value=WithPlugins.RuleImpl.class, priority=WithPlugins.PRIORITY)
public @interface WithPlugins {

    public static final int PRIORITY = 10;

    /**
     * See {@link PluginManager.PluginSpec} for the syntax.
     */
    String[] value();

    public class RuleImpl implements TestRule {

        private static final Logger LOGGER = Logger.getLogger(WithPlugins.class.getName());

        @Inject
        Jenkins jenkins;

        @Inject
        JenkinsController controller;

        @Inject @Named("ExercisedPluginReporter")
        ExercisedPluginsReporter pluginReporter;

        @Inject(optional=true) @Named("neverReplaceExistingPlugins")
        boolean neverReplaceExistingPlugins;

        @Override
        public Statement apply(final Statement base, final Description d) {
            return new Statement() {
                @Override
                public void evaluate() throws Throwable {
                    Set<String> plugins = new TreeSet<>();
                    boolean restartRequired = installPlugins(d.getAnnotation(WithPlugins.class), plugins);
                    restartRequired |= installPlugins(d.getTestClass().getAnnotation(WithPlugins.class), plugins);
                    LOGGER.info("for " + d + " asked to install " + plugins + "; restartRequired? " + restartRequired);
                    if (restartRequired) {
                        jenkins.restart();
                    }
                    for (String name : plugins) {
                        Plugin installedPlugin = jenkins.getPlugin(name);
                        VersionNumber installedVersion = installedPlugin.getVersion();
                        String version = installedVersion.toString();
                        pluginReporter.log(d.getClassName() + "." + d.getMethodName(), name, version);
                    }
                    base.evaluate();
                }
                @SuppressWarnings("deprecation")
                private boolean installPlugins(WithPlugins wp, Set<String> plugins) {
                    if (wp == null) return false;

                    PluginManager pm = jenkins.getPluginManager();
                    boolean restartRequired = false;
                    for (String c: wp.value()) {
                        PluginSpec candidate = new PluginSpec(c);
                        String name = candidate.getName();
                        plugins.add(name);

                        switch (pm.installationStatus(c)) {
                        case NOT_INSTALLED:
                            restartRequired |= pm.installPlugins(c);
                            break;
                        case OUTDATED:
                            if (neverReplaceExistingPlugins) {
                                throw new AssumptionViolatedException(String.format(
                                        "Test requires %s plugin", c));
                            } else {
                                restartRequired |= pm.installPlugins(c);
                            }
                        break;
                        default:
                            // OK
                        }
                    }
                    return restartRequired;
                }
            };
        }
    }
}
