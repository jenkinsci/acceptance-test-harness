package org.jenkinsci.test.acceptance.junit;

import org.junit.rules.TestRule;

/**
 * This is a service exception that wraps the TestRule failure to all traceability back to failing test rules.
 */
public class TestRuleException extends RuntimeException {
    //This is the test rule that failed.
    public final TestRule testRule;
    //We take the exception, as a throwable, and the test rule
    public TestRuleException(Throwable throwable, TestRule failingRule) {
        super(throwable);
        testRule = failingRule;
    }
    //This displays what the test rule that failed.
    @Override
    public String getMessage() {
        return "TestRule " + testRule.getClass() + " failed with stacktrace.\n" + super.getMessage();
    }
}
