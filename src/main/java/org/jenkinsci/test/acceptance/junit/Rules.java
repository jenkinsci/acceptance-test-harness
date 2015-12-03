package org.jenkinsci.test.acceptance.junit;

import org.junit.runner.Description;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.ServerSocket;

/**
 * Class providing miscellaneous support methods for JUnit rules.
 *
 * @author Andres Rodriguez
 */
final class Rules {
    /** Not instantiable. */
    private Rules() {
        throw new AssertionError("Not instantiable");
    }

    /** @return the test method or {@code null} if not applied to a method. */
    static Method getMethod(Description description) {
        final String methodName = description.getMethodName();
        if (methodName != null) {
            final Class<?> testClass = description.getTestClass();
            try {
                return testClass.getMethod(methodName);
            } catch(NoSuchMethodException e) {
                // Should not happen
                throw new AssertionError(String.format("Method [%s] not found in class [%s]", methodName, testClass));
            }
        }
        return null;
    }
}
