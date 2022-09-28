/*
 * The MIT License
 *
 * Copyright (c) 2015 Red Hat, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.jenkinsci.test.acceptance.junit;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.junit.AssumptionViolatedException;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Declare there is a property that needs to be provided to run the test.
 *
 * This is often used when credentials than can not be in git are needed
 * to run the test. Another use-case is test interacting with pre-deployed
 * service.
 *
 * Failing the activation criteria will skip the test but unlike JUnit assumptions
 * it will happen before Jenkins boots up.
 *
 * @author ogondza
 */
@Retention(RUNTIME)
@Target({METHOD, TYPE})
@Inherited
@Documented
@RuleAnnotation(value = TestActivation.RuleImpl.class, priority = -10)
public @interface TestActivation {

    /**
     * @return Property names that needs to be present to execute the test/test case.
     */
    String[] value();

    class RuleImpl implements TestRule {
        @Override
        public Statement apply(final Statement base, final Description d) {
            return new Statement() {
                @Override
                public void evaluate() throws Throwable {
                    Class<?> testCase = d.getTestClass();
                    check(d.getAnnotation(TestActivation.class), testCase);
                    check(testCase.getAnnotation(TestActivation.class), testCase);

                    base.evaluate();
                }

                private void check(TestActivation activation, Class<?> testClass) {
                    if (activation == null) return; // No activation - always run

                    String className = testClass.getSimpleName();

                    for (String property: activation.value()) {
                        String propertyName = className + "." + property;
                        if (System.getProperty(propertyName) == null) {
                            throw new AssumptionViolatedException(
                                    "Required property not provided: " + propertyName
                            );
                        }
                    }

                    // All properties provided - run
                }
            };
        }
    }
}
