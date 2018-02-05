package org.jenkinsci.test.acceptance.junit;

import org.junit.rules.TestRule;

public class TestRuleException extends RuntimeException {

    public final TestRule testRule;

    public TestRuleException(Throwable throwable, TestRule failingRule) {
        super(throwable);
        testRule = failingRule;
    }

    @Override
    public String getMessage() {
        return "TestRule " + testRule.getClass() + " failed with stacktrace.\n" + super.getMessage();
    }
}
