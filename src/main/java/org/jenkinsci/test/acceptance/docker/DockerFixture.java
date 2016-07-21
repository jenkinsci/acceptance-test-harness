package org.jenkinsci.test.acceptance.docker;

import org.jvnet.hudson.annotation_indexer.Indexed;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

/**
 * Annotates {@link DockerContainer} subtype that exposes fixture-specific methods.
 *
 * @author Kohsuke Kawaguchi
 * @author asotobueno
 */
@Target(TYPE)
@Retention(RUNTIME)
@Indexed
@Inherited
public @interface DockerFixture {

    public static final String DEFAULT_DOCKER_IP = "";
    
    /**
     * Unique ID of this fixture. Used from cucumber, etc. to find this annotation.
     */
    String id();

    /**
     * Ports that are exposed from this fixture.
     *
     * <p>
     * When a container is started, these ports from the container are mapped to
     * random ephemeral ports on the host. The actual ephemeral port number can
     * be retried at runtime via {@link DockerContainer#port(int)}.
     */
    int[] ports() default {};
    
    /**
     * Map container ports to host ports exactly.
     * 
     * <p>
     * If true, no random ephemeral ports will be used, but an exact matching of
     * container and host ports.
     */
    boolean matchHostPorts() default false;

    /**
     * Ip address to bind to
     * @deprecated this assumes you have network knowledge of the running ATH environment which you can not possibly have.
     *             Docker may be running on a remote machine and as such any address (e.g. <code>127.0.0.5<code>)
     *             you specify may not be reachable.
     */
    @Deprecated
    String bindIp() default DEFAULT_DOCKER_IP;

    /**
     * Path of Dockerfile file.
     *
     * <p>
     *  By default Dockerfile fixture should be placed in the resource directory
     *  and at the same package as DockerContainer subtype.
     * </p>
     * <p>
     *  If this attribute is present, Dockerfile folder specified in the attribute
     *  is used as Dockerfile fixture place.
     * </p>
     */
    String dockerfileFolder() default "";

}