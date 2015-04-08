package org.jenkinsci.test.acceptance.junit;

import com.google.inject.Inject;
import com.google.inject.Injector;

import org.jenkinsci.test.acceptance.controller.JenkinsController;
import org.jenkinsci.test.acceptance.guice.World;
import org.junit.internal.AssumptionViolatedException;
import org.junit.rules.MethodRule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.openqa.selenium.WebDriver;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeMap;

/**
 * Runs Guice container that houses {@link JenkinsController}, {@link WebDriver}, and so on.
 *
 * <p>
 * Add this rule to your Unit test class if you want to leverage this harness.
 *
 * <p>
 * This is the glue that connects JUnit to the logic of the test harness.
 *
 * @author Kohsuke Kawaguchi
 */
public class JenkinsAcceptanceTestRule implements MethodRule { // TODO should use TestRule instead
    @Override
    public Statement apply(final Statement base, final FrameworkMethod method, final Object target) {
        final Description description = Description.createTestDescription(method.getMethod().getDeclaringClass(), method.getName(), method.getAnnotations());
        return new Statement() {
            @Inject JenkinsController controller;
            @Inject Injector injector;

            @Override
            public void evaluate() throws Throwable {
                World world = World.get();
                Injector injector = world.getInjector();

                world.startTestScope(description.getDisplayName());

                injector.injectMembers(this);

                System.out.println("=== Starting " + description.getDisplayName());
                try {
                    decorateWithRules(base).evaluate();
                } catch (AssumptionViolatedException e) {
                    System.out.printf("Skipping %s: %s%n", description.getDisplayName(), e.getMessage());
                    throw e;
                } catch (Exception|AssertionError e) { // Errors and failures
                    controller.diagnose(e);
                    throw e;
                } finally {
                    world.endTestScope();
                }
            }

            /**
             * Look for annotations on a test and honor {@link RuleAnnotation}s in them.
             */
            private Statement decorateWithRules(Statement body) {
                Set<Class<? extends Annotation>> annotations = new HashSet<>();
                collectAnnotationTypes(method.getMethod(), annotations);
                collectAnnotationTypes(target.getClass(), annotations);

                TreeMap<Integer, Set<TestRule>> rules = new TreeMap<Integer, Set<TestRule>>(new Comparator<Integer>() {
                    @Override
                    public int compare(Integer o1, Integer o2) {
                        // Reversed since we apply the TestRule inside out:
                        return o2 - o1;
                    }
                });
                // Make sure Jenkins is started between -1 and 0
                rules.put(0, new LinkedHashSet<TestRule>());
                rules.get(0).add(jenkinsBoot(rules));

                for (Class<? extends  Annotation> a : annotations) {
                    RuleAnnotation r = a.getAnnotation(RuleAnnotation.class);
                    if (r!=null) {
                        int prio = r.priority();
                        if (rules.get(prio) == null) {
                            rules.put(prio, new LinkedHashSet<TestRule>());
                        }
                        rules.get(prio).add(injector.getInstance(r.value()));
                    }
                }

                for (Set<TestRule> rulesGroup: rules.values()) {
                    for (TestRule rule: rulesGroup) {
                        body = rule.apply(body, description);
                    }
                }
                return body;
            }

            private void collectAnnotationTypes(AnnotatedElement e, Collection<Class<? extends Annotation>> types) {
                for (Annotation a : e.getAnnotations()) {
                    types.add(a.annotationType());
                }
            }

            private TestRule jenkinsBoot(final TreeMap<Integer, Set<TestRule>> rules) {
                return new TestRule() {
                    @Override
                    public Statement apply(final Statement base, Description description) {
                        return new Statement() {
                            @Override public void evaluate() throws Throwable {
                                controller.start();
                                // Now it is safe to inject Jenkins
                                injector.injectMembers(target);
                                for (Set<TestRule> rg: rules.values()) {
                                    for (TestRule rule: rg) {
                                        injector.injectMembers(rule);
                                    }
                                }
                                base.evaluate();
                            }
                        };
                    }
                };
            }
        };
    }
}
