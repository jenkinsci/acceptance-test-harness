package org.jenkinsci.test.acceptance.plugins.analysis_core;

import org.jenkinsci.test.acceptance.po.PostBuildStep;

/**
 * Null object.
 *
 * @author Ullrich Hafner
 */
public class NullConfigurator implements AnalysisConfigurator {
    @Override
    public void accept(PostBuildStep settings) {
        // nothing to do
    }
}
