package org.jenkinsci.test.acceptance.steps;

import org.jenkinsci.test.acceptance.po.CapybaraPortingLayer;
import org.jenkinsci.test.acceptance.po.Jenkins;

import javax.inject.Inject;

/**
 * @author Kohsuke Kawaguchi
 */
public abstract class AbstractSteps extends CapybaraPortingLayer {
    /**
     * Implicit contextual Jenkins instance.
     */
    @Inject
    Jenkins jenkins;

}
