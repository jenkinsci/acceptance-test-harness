package org.jenkinsci.test.acceptance.guice;

import org.junit.runners.model.Statement;

import java.io.Closeable;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Performs clean-up tasks at the end of scope.
 *
 * Tests and their decorators can add stuff to this cleaner to ensure some cleanup operation
 * happens at the end of each test.
 *
 * @author Kohsuke Kawaguchi
 */
public class Cleaner {
    private static final Logger LOGGER = Logger.getLogger(Cleaner.class.getName());
    private final Deque<Statement> tasks = new ArrayDeque<>();

    public void addTask(Statement stmt) {
        tasks.push(stmt);
    }

    public void addTask(final Runnable r) {
        addTask(new Statement() {
            @Override
            public void evaluate() throws Throwable {
                r.run();
            }

            @Override
            public String toString() {
                return r.toString();
            }
        });
    }

    public void addTask(final Closeable c) {
        addTask(new Statement() {
            @Override
            public void evaluate() throws Throwable {
                c.close();
            }

            @Override
            public String toString() {
                return c.toString();
            }
        });
    }

    public void addTask(final Callable<?> c) {
        addTask(new Statement() {
            @Override
            public void evaluate() throws Throwable {
                c.call();
            }

            @Override
            public String toString() {
                return c.toString();
            }
        });
    }
    public void performCleanUp() {
        LOGGER.info("Performing cleanup tasks in order: " + tasks);
        for (Statement task : tasks) {
            try {
                task.evaluate();
            } catch (Throwable t) {
                LOGGER.log(Level.SEVERE, task + " failed", t);
            }
        }
        tasks.clear();
    }
}
