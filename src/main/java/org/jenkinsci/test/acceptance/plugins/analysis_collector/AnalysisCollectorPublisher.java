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
    public final Control isFindbugsActivated = control("isFindBugsActivated");


    public AnalysisCollectorPublisher(Job parent, String path) {
        super(parent, path);
    }

    /**
     * Select if the warnings of given plugin should be collected by Analysis Collector Plugin.
     *
     * @param plugin  the Plugin
     * @param checked true or false
     */
    public void checkCollectedPlugin(AnalysisPlugin plugin, boolean checked) {
        switch (plugin) {
            case CHECKSTYLE:
                isCheckstyleActivated.check(checked);
                break;
            case DRY:
                isDryActivated.check(checked);
                break;
            case PMD:
                isPmdActivated.check(checked);
                break;
            case FINDBUGS:
                isFindbugsActivated.check(checked);
                break;
            case TASKS:
                isOpenTasksActivated.check(checked);
                break;
            case WARNINGS:
                isWarningsActivated.check(checked);
                break;
            default:
                break;
        }
    }

    public enum AnalysisPlugin {
        CHECKSTYLE,
        DRY,
        PMD,
        FINDBUGS,
        TASKS,
        WARNINGS;
    }

}
