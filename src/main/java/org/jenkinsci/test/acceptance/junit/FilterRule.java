/*
 * The MIT License
 *
 * Copyright (c) 2014 Red Hat, Inc.
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

import com.google.inject.Inject;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.jenkinsci.test.acceptance.guice.World;
import org.junit.Assume;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Test rule to filter tests to run.
 *
 * @author ogondza
 */
@GlobalRule(priority = -1) // Run before Jenkins is started
public class FilterRule implements TestRule {

    @Override
    public Statement apply(final Statement base, final Description description) {
        return new Statement() {

            @Inject(optional = true)
            Filter filter;

            @Override
            public void evaluate() throws Throwable {
                World.get().getInjector().injectMembers(this);
                if (filter != null) {
                    String reason = filter.whySkip(base, description);
                    // Fail assumption if there is some reason to skip
                    Assume.assumeTrue(reason, reason == null);
                }

                base.evaluate();
            }
        };
    }

    public abstract static class Filter {
        /**
         * @return null if test should be run, the reason why not otherwise.
         */
        public abstract String whySkip(Statement base, Description description);

        public static <T extends Annotation> Set<T> getAnnotations(Description description, Class<T> type) {
            Set<T> annotations = new HashSet<T>();
            annotations.add(description.getAnnotation(type));
            annotations.add(description.getTestClass().getAnnotation(type));
            annotations.remove(null);
            return annotations;
        }

        public static Set<Annotation> getAnnotations(Description description) {
            Set<Annotation> annotations = new HashSet<Annotation>();
            annotations.addAll(description.getAnnotations());
            annotations.addAll(Arrays.asList(description.getTestClass().getAnnotations()));
            return annotations;
        }
    }
}
