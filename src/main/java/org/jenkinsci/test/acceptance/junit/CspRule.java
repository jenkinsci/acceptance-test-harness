package org.jenkinsci.test.acceptance.junit;

import com.google.inject.Inject;
import com.google.inject.Injector;
import java.util.Map;
import java.util.logging.Level;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.jenkinsci.test.acceptance.po.JenkinsLogger;
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
                    jenkins.createLogger("CSP", Map.of("jenkins.security.csp.impl.LoggingReceiver", Level.FINEST));
                }
                base.evaluate();
                if (isEnabled() && !isSkipped()) {
                    JenkinsLogger logger = new JenkinsLogger(jenkins, "CSP");
                    if (!logger.isEmpty()) {
                        throw new AssertionError("CSP violations were logged during the test: " + logger.getAllMessages());
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
