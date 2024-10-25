package org.jenkinsci.test.acceptance.junit;

import com.google.inject.Inject;
import com.google.inject.Injector;
import org.jenkinsci.test.acceptance.po.GlobalSecurityConfig;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.jenkinsci.test.acceptance.update_center.PluginSpec;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

@GlobalRule
public final class CspRule implements TestRule {

    @Inject
    private Injector injector;

    @Override
    public Statement apply(final Statement base, final Description d) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                if (isEnabled()
                        && d.getAnnotation(WithInstallWizard.class) == null
                        && d.getTestClass().getAnnotation(WithInstallWizard.class) == null) {
                    Jenkins jenkins = injector.getInstance(Jenkins.class);

                    PluginSpec plugin = new PluginSpec("csp");
                    jenkins.getPluginManager().installPlugins(plugin);

                    GlobalSecurityConfig security = new GlobalSecurityConfig(jenkins);
                    security.open();
                    security.disableCspReportOnly();
                    security.save();
                }
                base.evaluate();
            }

            private static boolean isEnabled() {
                if (System.getProperty("csp.rule") == null) {
                    return false;
                }
                if (System.getProperty("csp.rule").isEmpty()) {
                    return true;
                }
                return Boolean.getBoolean("csp.rule");
            }
        };
    }
}
