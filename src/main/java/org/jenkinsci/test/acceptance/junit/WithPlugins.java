package org.jenkinsci.test.acceptance.junit;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import org.jenkinsci.test.acceptance.controller.JenkinsController;
import org.jenkinsci.test.acceptance.guice.LocalPluginOverride;
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

import java.io.File;
import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

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
@RuleAnnotation(WithPlugins.RuleImpl.class)
public @interface WithPlugins {
    /**
     * See {@link PluginManager.PluginSpec} for the syntax.
     */
    String[] value();

    public class RuleImpl implements TestRule {
        @Inject
        Jenkins jenkins;

        @Inject
        JenkinsController controller;

        @Inject @Named("ExercisedPluginReporter")
        ExercisedPluginsReporter pluginReporter;

        @Inject(optional=true) @Named("neverReplaceExistingPlugins")
        boolean neverReplaceExistingPlugins;

        @Inject // TODO: generalize this extension to allow anything that can install plugins
        LocalPluginOverride override;

        @Override
        public Statement apply(final Statement base, final Description d) {
            return new Statement() {
                @Override
                public void evaluate() throws Throwable {
                    installPlugins(d.getAnnotation(WithPlugins.class));
                    installPlugins(d.getTestClass().getAnnotation(WithPlugins.class));

                    base.evaluate();
                }

                private boolean installPlugins(WithPlugins wp) {
                    if (wp == null) return false;

                    PluginManager pm = jenkins.getPluginManager();
                    for (String c: wp.value()) {
                        PluginSpec candidate = new PluginSpec(c);
                        String name = candidate.getName();

                        if (!pm.isInstalled(name)) {
                            File override = RuleImpl.this.override.get(name);
                            if (override != null) {
                                pm.installPlugin(override);
                            } else {
                                pm.installPlugins(name);
                            }
                        } else {
                            String requiredVersion = candidate.getVersion();
                            if (requiredVersion != null) {
                                if (!jenkins.getPlugin(name).isNewerThan(requiredVersion)) {
                                    if (neverReplaceExistingPlugins) {
                                        throw new AssumptionViolatedException(String.format(
                                                "Test requires %s plugin in version %s", name, requiredVersion));
                                    } else {
                                        pm.installPlugins(c);
                                    }
                                }
                            }
                        }
                        Plugin installedPlugin = jenkins.getPlugin(name);
                        VersionNumber installedVersion = installedPlugin.getVersion();
                        String version = installedVersion.toString();

                        pluginReporter.log(d.getClassName() + "." + d.getMethodName(), name, version);

                    }
                    return true;
                }
            };
        }
    }
}
