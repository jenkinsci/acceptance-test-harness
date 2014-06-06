package org.jenkinsci.test.acceptance.steps;

import javax.inject.Inject;

import org.jenkinsci.test.acceptance.guice.TestScope;
import org.jenkinsci.test.acceptance.po.CapybaraPortingLayerImpl;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.openqa.selenium.WebDriver;

/**
 * Base class for cucumber step definitions. Injects several fields that are most frequently used, and extend from
 * {@link org.jenkinsci.test.acceptance.po.CapybaraPortingLayerImpl} so that {@link WebDriver} operations can be called
 * directly in subtypes.
 *
 * @author Kohsuke Kawaguchi
 */
@TestScope
public abstract class AbstractSteps extends CapybaraPortingLayerImpl {
    /**
     * Implicit contextual Jenkins instance.
     */
    @Inject
    Jenkins jenkins;

    /**
     * Contextual variables.
     */
    @Inject
    Context my;

    protected AbstractSteps() {
        super(null);
    }
}
