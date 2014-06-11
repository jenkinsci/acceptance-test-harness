package org.jenkinsci.test.acceptance.plugins.analysis_collector;

import org.jenkinsci.test.acceptance.plugins.AbstractCodeStylePluginPostBuildStep;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.Job;

/**
 * @author Michael Prankl
 */
@Describable("Publish combined analysis results")
public class AnalysisCollectorPublisher extends AbstractCodeStylePluginPostBuildStep {

    public final Control isCheckstyleActivated = control("isCheckStyleActivated");
    public final Control isDryActivated = control("isDryActivated");
    public final Control isPmdActivated = control("isPmdActivated");
    public final Control isOpenTasksActivated = control("isOpenTasksActivated");
    public final Control isWarningsActivated = control("isWarningsActivated");


    public AnalysisCollectorPublisher(Job parent, String path) {
        super(parent, path);
    }
}
