package org.jenkinsci.test.acceptance.docker;

import com.google.inject.Inject;
import com.google.inject.TypeLiteral;
import org.jenkinsci.test.acceptance.guice.AutoCleaned;
import org.jenkinsci.test.acceptance.guice.TestCleaner;
import org.jenkinsci.test.acceptance.guice.TestScope;

import javax.inject.Provider;
import java.io.Closeable;
import java.io.IOException;

/**
 * Inject this object to automate the cleanup of a running container at the end of the test case.
 *
 * @author Kohsuke Kawaguchi
 */
@TestScope
public class DockerContainerHolder<T extends DockerContainer> implements Provider<T>, Closeable, AutoCleaned {
    @Inject
    TypeLiteral<T> type;

    @Inject
    Docker docker;

    T container;

    /**
     * Lazily starts a container and returns the instance.
     */
    public synchronized T get() {
        if (container==null)
            container = docker.start((Class<T>)type.getRawType());
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
