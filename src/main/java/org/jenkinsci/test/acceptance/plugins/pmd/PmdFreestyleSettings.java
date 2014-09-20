package org.jenkinsci.test.acceptance.plugins.pmd;

import org.jenkinsci.test.acceptance.plugins.analysis_core.AnalysisFreestyleSettings;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.Job;

/**
 * @author Kohsuke Kawaguchi
 */
@Describable("Publish PMD analysis results")
public class PmdFreestyleSettings extends AnalysisFreestyleSettings {
    public PmdFreestyleSettings(Job parent, String path) {
        super(parent, path);
    }
}
