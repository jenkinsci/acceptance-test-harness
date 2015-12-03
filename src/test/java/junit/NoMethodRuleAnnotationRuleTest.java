package junit;

import org.jenkinsci.test.acceptance.junit.NoMethodRuleAnnotationRule;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

/**
 * Test for {@link NoMethodRuleAnnotationRule}.
 *
 * @author Andres Rodriguez
 */
public class NoMethodRuleAnnotationRuleTest {
    /** Rule to test the rule. */
    public TestRule rule() {
        final ExpectedException outer = ExpectedException.none();
        outer.expect(IllegalStateException.class);
        return RuleChain.outerRule(outer).around(NoMethodRuleAnnotationRule.rule());
    }

    /** Singleton instance. */
    private static final NoMethodRuleAnnotationRuleTest RULE = new NoMethodRuleAnnotationRuleTest();

    /** Test method. Should throw {@link IllegalStateException}. */
    @Test
    @WithPlugins("dummy-value")
    public void notAllowed() {
    }
}
