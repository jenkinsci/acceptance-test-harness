package org.jenkinsci.test.acceptance.guice;

import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Scope;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Keeps track of {@link TestScope} objects.
 *
 * @author Kohsuke Kawaguchi
 */
public class TestLifecycle implements Scope {
    /**
     * Records components that are scoped to tests.
     * <p>
     * Inherited, so that threads created from within a test can correctly identify its scope.
     */
    private final ThreadLocal<Map> testScopeObjects = new InheritableThreadLocal<>();

    /**
     * Call this method when a new test starts, to reset the {@link TestScope}.
     */
    public void startTestScope() {
        testScopeObjects.set(new HashMap());
    }

    public void endTestScope() {
        testScopeObjects.set(null);
    }

    public Map export() {
        Map o = testScopeObjects.get();
        testScopeObjects.set(null);
        return o;
    }

    public void import_(Map o) {
        testScopeObjects.set(o);
    }

    /**
     * Returns already existing instances.
     */
    /*package*/ Collection<Object> getInstances() {
        return testScopeObjects.get().values();
    }

    @Override
    public <T> Provider<T> scope(final Key<T> key, final Provider<T> base) {
        return () -> {
            Map m = testScopeObjects.get();
            if (m == null) {
                return null;
            }
            T v = (T) m.get(key);
            if (v == null) {
                m.put(key, v = base.get());
            }
            return v;
        };
    }
}
