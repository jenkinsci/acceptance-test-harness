package org.jenkinsci.test.acceptance.plugins.pmd;

import org.jenkinsci.test.acceptance.plugins.analysis_core.AnalysisFreestyleSettings;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.Job;

/**
 * PMD build settings for freestyle projects.
 *
 * @author Fabian Trampusch
 */
@Describable("Publish PMD analysis results")
public class PmdFreestyleSettings extends AnalysisFreestyleSettings {
    public PmdFreestyleSettings(final Job parent, final String path) {
        super(parent, path);
    }
}
