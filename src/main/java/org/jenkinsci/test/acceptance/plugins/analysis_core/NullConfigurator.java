package org.jenkinsci.test.acceptance.plugins.analysis_core;

import org.jenkinsci.test.acceptance.po.PostBuildStep;

/**
 * Null object.
 *
 * @author Ullrich Hafner
 */
public class NullConfigurator<T extends PostBuildStep>  implements AnalysisConfigurator<T> {
    @Override
    public void accept(T analysisSettings) {
        // nothing to do
    }
}
