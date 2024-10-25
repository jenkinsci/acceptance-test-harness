package org.jenkinsci.test.acceptance.junit;

import com.google.inject.Inject;
import com.google.inject.Injector;
import java.util.List;
import java.util.logging.Logger;
import org.jenkinsci.test.acceptance.plugins.csp.ContentSecurityPolicyReport;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.jenkinsci.test.acceptance.update_center.PluginSpec;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

@GlobalRule
public final class CspRule implements TestRule {

    private static final Logger LOGGER = Logger.getLogger(CspRule.class.getName());

    @Inject
    Injector injector;

    @Override
    public Statement apply(final Statement base, final Description d) {
        return new Statement() {
            private Jenkins jenkins;

            @Override
            public void evaluate() throws Throwable {
                jenkins = injector.getInstance(Jenkins.class);
                final PluginSpec plugin = new PluginSpec("csp");
                LOGGER.info("Installing plugin for test: " + plugin);
                jenkins.getPluginManager().installPlugins(plugin);
                try {
                    base.evaluate();
                } finally {
                    ContentSecurityPolicyReport csp = new ContentSecurityPolicyReport(jenkins);
                    List<String> lines = csp.getReport();
                    if (lines.size() > 2) {
                        throw new AssertionError(String.join("\n", csp.getReport()));
                    }
                }
            }
        };
    }
}
