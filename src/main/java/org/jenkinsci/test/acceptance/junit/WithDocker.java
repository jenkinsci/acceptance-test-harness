package org.jenkinsci.test.acceptance.junit;

import com.google.inject.Inject;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.jenkinsci.test.acceptance.docker.Docker;
import org.jenkinsci.test.acceptance.docker.DockerImage;
import org.junit.AssumptionViolatedException;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Indicates the docker is necessary to run the test.
 * <p>
 * Otherwise the test is skipped.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Inherited
@Documented
@RuleAnnotation(value = WithDocker.RuleImpl.class, priority = -10) // Run before Jenkins startup
public @interface WithDocker {
    
    /**
     * Set to true if the test requires the docker deamon to be running locally. 
     */
    boolean localOnly() default false;
    
    class RuleImpl implements TestRule {
        @Inject
        Docker docker;

        @Override
        public Statement apply(final Statement base, final Description d) {
            return new Statement() {
                @Override
                public void evaluate() throws Throwable {
                    hasDocker(d.getAnnotation(WithDocker.class));
                    hasDocker(d.getTestClass().getAnnotation(WithDocker.class));
                    base.evaluate();
                }

                private void hasDocker(WithDocker n) throws UnknownHostException {
                    if (n==null) return;

                    if (!docker.isAvailable()) {
                        throw new AssumptionViolatedException("Docker is needed for the test");
                    }
                    if (n.localOnly()) {
                        String host = DockerImage.getDockerHost();
                        if (!InetAddress.getByName(host).isLoopbackAddress()) {
                            throw new AssumptionViolatedException("Docker is needed locally for the test but is running on " + host);
                        }
                    }
                }
            };
        }
    }
}
