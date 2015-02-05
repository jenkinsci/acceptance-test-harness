package org.jenkinsci.test.acceptance.plugins.checkstyle;

import org.jenkinsci.test.acceptance.plugins.analysis_core.AnalysisFreestyleSettings;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.Job;

/**
 * CheckStyle build settings for freestyle projects.
 *
 * @author Fabian Trampusch
 */
@Describable("Publish Checkstyle analysis results")
public class CheckStyleFreestyleSettings extends AnalysisFreestyleSettings {
    public CheckStyleFreestyleSettings(final Job parent, final String path) {
        super(parent, path);
    }
}
