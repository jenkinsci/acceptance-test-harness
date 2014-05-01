package org.jenkinsci.test.acceptance.plugins;

import org.jenkinsci.test.acceptance.po.PostBuildStep;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Job;

/**
 * Abstract class to use for post build steps.
 * @author Martin Kurz
 */
public abstract class AbstractCodeStylePluginPostBuildStep extends PostBuildStep {
    public final Control pattern = control("pattern");

    public AbstractCodeStylePluginPostBuildStep(Job parent, String path) {
        super(parent, path);
    }
}
