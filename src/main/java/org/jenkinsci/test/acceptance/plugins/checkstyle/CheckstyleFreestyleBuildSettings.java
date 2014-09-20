package org.jenkinsci.test.acceptance.plugins.checkstyle;

import org.jenkinsci.test.acceptance.plugins.analysis_core.AnalysisFreestyleSettings;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.Job;

/**
 * @author Kohsuke Kawaguchi
 */
@Describable("Publish Checkstyle analysis results")
public class CheckstyleFreestyleBuildSettings extends AnalysisFreestyleSettings {
    public CheckstyleFreestyleBuildSettings(Job parent, String path) {
        super(parent, path);
    }
}
