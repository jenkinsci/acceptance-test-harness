package org.jenkinsci.test.acceptance.junit;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.jenkinsci.test.acceptance.controller.JenkinsController;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.jenkinsci.test.acceptance.po.PluginManager;
import org.jenkinsci.test.acceptance.po.PluginManager.PluginCoordinates;
import org.junit.internal.AssumptionViolatedException;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import com.google.inject.Inject;

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
    String[] value();

    public class RuleImpl implements TestRule {
        @Inject
        Jenkins jenkins;

        @Inject
        JenkinsController controller;

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
                        PluginCoordinates candidate = new PluginManager.PluginCoordinates(c);
                        String name = candidate.getName();

                        if (!pm.isInstalled(name)) {
                            pm.installPlugins(name);
                        }
                        else {
                            String requiredVersion = candidate.getVersion();
                            if (requiredVersion != null) {
                                if (!jenkins.getPlugin(name).isNewerThan(requiredVersion)) {
                                    if (System.getenv("NEVER_REPLACE_EXISTING_PLUGINS") != null) {
                                        throw new AssumptionViolatedException(String.format(
                                                "Test requires %s plugin in version %s", name, requiredVersion));
                                    }
                                    else {
                                        pm.installPlugins(name);
                                    }
                                }
                            }
                        }
                    }
                    return true;
                }
            };
        }
    }
}
