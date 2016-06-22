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
    private int portOffset = 0;

    /**
     * Lazily starts a container and returns the instance.
     */
    @Override
    public synchronized T get() {
        if (container==null) {
            Class<T> fixture = (Class<T>) type.getRawType();
            File buildlog = diag.touch("docker-" + fixture.getSimpleName() + ".build.log");
            File runlog = diag.touch("docker-" + fixture.getSimpleName() + ".run.log");
            Exception launchException = null;
            boolean keepTrying = true;
            int i = 0;
            for (; i < 5 && keepTrying; i++) {
                try {
                    container = docker.build(fixture, buildlog).start(fixture).withPortOffset(portOffset).withLog(runlog).start();
                    launchException = null;
                    break;
                } catch (InterruptedException | IOException e) {
                    launchException = e;
                    // Only keep trying if the error is cidFile related.
                    if (!e.getMessage().contains("docker didn't leave CID file")) {
                        keepTrying = false;
                    }
                }
                try {
                    // If we've got this far, that means there was a failure - sleep for 5 seconds and try again.
                    Thread.sleep(5000);
                } catch (InterruptedException e2) {
                    // Swallow it
                }
            }

            if (launchException != null) {
                throw new Error("Failed to start container after " + i + " tries - " + fixture.getName(), launchException);
            }
        }
        return container;
    }

    /**
     * {@link TestCleaner} will call this at the end of the test automatically
     */
    @Override
    public void close() throws IOException {
        if (container!=null) {
            container.close();
            container = null;
        }
    }
}
