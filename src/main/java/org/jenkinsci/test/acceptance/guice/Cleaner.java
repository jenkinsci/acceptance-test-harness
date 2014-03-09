package org.jenkinsci.test.acceptance.guice;

import org.junit.runners.model.Statement;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Performs clean-up tasks at the end of scope.
 *
 * Tests and their decorators can add stuff to this cleaner to ensure some cleanup operation
 * happens at the end of each test.
 *
 * @author Kohsuke Kawaguchi
 */
public class Cleaner {
    private final List<Statement> tasks = new ArrayList<>();

    public void addTask(Statement stmt) {
        tasks.add(stmt);
    }

    public void addTask(final Runnable r) {
        addTask(new Statement() {
            @Override
            public void evaluate() throws Throwable {
                r.run();
            }
        });
    }

    public void addTask(final Closeable c) {
        addTask(new Statement() {
            @Override
            public void evaluate() throws Throwable {
                c.close();
            }
        });
    }

    public void addTask(final Callable<?> c) {
        addTask(new Statement() {
            @Override
            public void evaluate() throws Throwable {
                c.call();
            }
        });
    }
    /*package*/ void performCleanUp() {
        for (Statement task : tasks) {
            try {
                task.evaluate();
            } catch (Throwable t) {
                throw new AssertionError(task+" failed",t);
            }
        }
    }
}
