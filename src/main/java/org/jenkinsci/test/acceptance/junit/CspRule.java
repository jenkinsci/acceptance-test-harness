package org.jenkinsci.test.acceptance.junit;

import com.google.inject.Inject;
import com.google.inject.Injector;
import java.util.List;
import org.jenkinsci.test.acceptance.plugins.csp.ContentSecurityPolicyReport;
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
                Jenkins jenkins = injector.getInstance(Jenkins.class);

                if (isEnabled() && !isSkipped()) {
                    PluginSpec plugin = new PluginSpec("csp");
                    jenkins.getPluginManager().installPlugins(plugin);

                    GlobalSecurityConfig security = new GlobalSecurityConfig(jenkins);
                    security.open();
                    security.disableCspReportOnly();
                    security.save();
                }
                try {
                    base.evaluate();
                } finally {
                    // TODO enable for ArtifactoryPluginTest when JENKINS-74047 is resolved
                    // TODO enable for LogParserTest when JENKINS-74890 is resolved
                    if (isEnabled()
                            && !isSkipped()
                            && !d.getTestClass().getName().equals("plugins.ArtifactoryPluginTest")
                            && !d.getTestClass().getName().equals("plugins.LogParserTest")) {
                        ContentSecurityPolicyReport csp = new ContentSecurityPolicyReport(jenkins);
                        csp.open();
                        List<String> lines = csp.getReport();
                        if (lines.size() > 2) {
                            throw new AssertionError(String.join("\n", lines));
                        }
                    }
                }
            }

            private boolean isSkipped() {
                return d.getAnnotation(WithInstallWizard.class) != null
                        || d.getTestClass().getAnnotation(WithInstallWizard.class) != null;
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
