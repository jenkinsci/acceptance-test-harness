package org.jenkinsci.test.acceptance.docker;

import com.google.inject.Inject;
import com.google.inject.TypeLiteral;
import org.jenkinsci.test.acceptance.guice.AutoCleaned;
import org.jenkinsci.test.acceptance.guice.TestCleaner;
import org.jenkinsci.test.acceptance.guice.TestScope;
import org.jenkinsci.test.acceptance.junit.FailureDiagnostics;

import javax.inject.Named;
import javax.inject.Provider;
import java.io.File;
import java.io.IOException;
import org.jenkinsci.test.acceptance.docker.DockerImage.Starter;
import org.jenkinsci.test.acceptance.docker.fixtures.FtpdContainer;

/**
 * Inject this object to automate the cleanup of a running container at the end of the test case.
 *
 * @author Kohsuke Kawaguchi
 */
@TestScope
public class DockerContainerHolder<T extends DockerContainer> implements Provider<T>, AutoCleaned {
    @Inject
    TypeLiteral<T> type;

    @Inject
    Docker docker;

    @Inject
    private FailureDiagnostics diag;

    T container;

    /**
     * Injecting a portOffset will force the binding of dockerPorts to local Ports with an offset
     * (e.g. bind docker 22 to localhost port 40022,
     */
    @Inject(optional = true)
    @Named("dockerPortOffset")
    private Integer portOffset;

    /**
     * Lazily starts a container and returns the instance.
     */
    @Override
    public synchronized T get() {
        if (container==null) {
            Class<T> fixture = (Class<T>) type.getRawType();
            File buildlog = diag.touch("docker-" + fixture.getSimpleName() + ".build.log");
            File runlog = diag.touch("docker-" + fixture.getSimpleName() + ".run.log");
            try {
                Starter<T> containerStarter = docker.build(fixture, buildlog).start(fixture);
                if (portOffset != null) containerStarter.withPortOffset(portOffset);
                container = containerStarter.withLog(runlog).start();
            } catch (InterruptedException | IOException e) {
                throw new Error("Failed to start container - " + fixture.getName(), e);
            }
        }
        return container;
    }

    /**
     * {@link TestCleaner} will call this at the end of the test automatically
     */
    @Override
    public void close() throws IOException {
        if (container != null) {
            container.close();
            container = null;
        }
    }
}
