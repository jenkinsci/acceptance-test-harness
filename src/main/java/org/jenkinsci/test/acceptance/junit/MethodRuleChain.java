// Copyright JUnit project. Copy-pasted from org.junit.rules.RuleChain.
package org.jenkinsci.test.acceptance.junit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.rules.MethodRule;
import org.junit.rules.RuleChain;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

/**
 * {@link MethodRule} version of {@link RuleChain}.
 */
public class MethodRuleChain implements MethodRule {
    private static final MethodRuleChain EMPTY_CHAIN= new MethodRuleChain(
            Collections.<MethodRule> emptyList());

    private List<MethodRule> rulesStartingWithInnerMost;

    public static MethodRuleChain emptyRuleChain() {
        return EMPTY_CHAIN;
    }

    public static MethodRuleChain outerRule(MethodRule outerRule) {
        return emptyRuleChain().around(outerRule);
    }

    private MethodRuleChain(List<MethodRule> rules) {
        this.rulesStartingWithInnerMost= rules;
    }

    public MethodRuleChain around(MethodRule enclosedRule) {
        List<MethodRule> rulesOfNewChain= new ArrayList<MethodRule>();
        rulesOfNewChain.add(enclosedRule);
        rulesOfNewChain.addAll(rulesStartingWithInnerMost);
        return new MethodRuleChain(rulesOfNewChain);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Statement apply(Statement base, FrameworkMethod method, Object target) {
        for (MethodRule each : rulesStartingWithInnerMost)
            base= each.apply(base, method, target);
        return base;
    }
}
