package org.jenkinsci.test.acceptance.junit;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

/**
 * Reports failures immediately.
 *
 * @author Ullrich Hafner
 */
public class JUnitProgressReporter extends RunListener {
    private final Set<String> results = new CopyOnWriteArraySet<>();

    @Override
    public void testStarted(final Description description) throws Exception {
        System.out.println("=== Starting test " + getSuffix(description));
    }

    private String getSuffix(final Object object) {
        return object + ": " + new SimpleDateFormat("HH:mm:ss").format(new Date());
    }

    @Override
    public void testFinished(final Description description) throws Exception {
        if (!results.contains(description.toString())) {
            System.out.println("+++ Test successful!");
        }
    }

    @Override
    public void testFailure(final Failure failure) throws Exception {
        System.out.println("--- Test failed: " + getSuffix(failure));

        results.add(failure.getDescription().toString());
    }

    @Override
    public void testAssumptionFailure(final Failure failure) {
        System.out.println("--- Assumption failed: " + getSuffix(failure));

        results.add(failure.getDescription().toString());
    }
}
