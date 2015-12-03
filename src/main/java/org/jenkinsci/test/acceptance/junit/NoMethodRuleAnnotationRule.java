package org.jenkinsci.test.acceptance.junit;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * JUnit rule that forbids using {@link RuleAnnotation} in methods.
 *
 * @author Andres Rodriguez
 */
public class NoMethodRuleAnnotationRule implements TestRule {
    /** Singleton instance. */
    private static final NoMethodRuleAnnotationRule RULE = new NoMethodRuleAnnotationRule();

    /** @return Factory method. */
    public static NoMethodRuleAnnotationRule rule() {
        return RULE;
    }

    /** Private constructor (use factory method). */
    private NoMethodRuleAnnotationRule() {
    }

    /** Checks if a method has {@link RuleAnnotation}s. */
    private boolean methodHasRuleAnnotations(@Nullable Method method) {
        if (method != null) {
            for (Annotation a : method.getAnnotations()) {
                if (a.annotationType().isAnnotationPresent(RuleAnnotation.class)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public Statement apply(final Statement base, final Description description) {
        final Method method = Rules.getMethod(description);
        if (!methodHasRuleAnnotations(method)) {
            return base; // nothing to do
        }
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                throw new IllegalStateException(String.format("Method %s cannot use RuleAnnotations", method.getName()));
            }
        };
    }
}
