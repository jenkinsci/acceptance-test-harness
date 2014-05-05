package org.jenkinsci.test.acceptance.plugins;

import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Job;
import org.jenkinsci.test.acceptance.po.PostBuildStepImpl;

/**
 * Abstract class to use for post build steps.
 *
 * @author Martin Kurz
 */
public abstract class AbstractCodeStylePluginPostBuildStep extends PostBuildStepImpl {
    public final Control pattern = control("pattern");

    public AbstractCodeStylePluginPostBuildStep(Job parent, String path) {
        super(parent, path);
    }
}
