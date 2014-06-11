package org.jenkinsci.test.acceptance.guice;

import com.cloudbees.sdk.extensibility.Extension;
import com.cloudbees.sdk.extensibility.ExtensionModule;
import com.google.inject.AbstractModule;
import org.junit.rules.TestName;

/**
 * Defines {@link TestScope} and exposes {@link TestLifecycle} to clean-up test-scoped instances.
 *
 * @author Kohsuke Kawaguchi
 */
@Extension
public class TestScopeModule extends AbstractModule implements ExtensionModule {
    @Override
    protected void configure() {
        TestLifecycle tl = new TestLifecycle();
        bindScope(TestScope.class, tl);
        bind(TestLifecycle.class).toInstance(tl);
    }
}
