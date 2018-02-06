package org.jenkinsci.test.acceptance.junit;

import org.junit.rules.TestRule;

/**
 * This is a service exception that wraps the TestRule failure to all traceability back to failing test rules.
 */
public class RuleFailedException extends RuntimeException {
    private static final long serialVersionUID = 183080398115494371L;

    private final String failedRule;

    public RuleFailedException(Throwable throwable, TestRule failingRule) {
        super(throwable);
        failedRule = failingRule.getClass().getName();
    }

    @Override
    public String getMessage() {
        return "TestRule " + failedRule + " failed: " + super.getMessage();
    }
}
