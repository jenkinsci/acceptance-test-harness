package org.jenkinsci.test.acceptance.steps;

import org.jenkinsci.test.acceptance.guice.TestScope;
import org.jenkinsci.test.acceptance.po.CapybaraPortingLayer;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.openqa.selenium.WebDriver;

import javax.inject.Inject;

/**
 * Base class for cucumber step definitions. Injects several fields that are
 * most frequently used, and extend from {@link CapybaraPortingLayer} so that
 * {@link WebDriver} operations can be called directly in subtypes.
 *
 *
 * @author Kohsuke Kawaguchi
 */
@TestScope
public abstract class AbstractSteps extends CapybaraPortingLayer {
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
