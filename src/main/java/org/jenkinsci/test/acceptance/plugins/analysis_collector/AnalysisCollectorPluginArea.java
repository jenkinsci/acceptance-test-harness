package org.jenkinsci.test.acceptance.plugins.analysis_collector;

import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.PageAreaImpl;
import org.jenkinsci.test.acceptance.po.PageObject;

/**
 * Page Area where the Analysis Plugins which should be included in the collection can be checked.
 * @author Michael Prankl
 */
public class AnalysisCollectorPluginArea extends PageAreaImpl {

    public final Control isCheckstyleActivated = control("isCheckStyleActivated");
    public final Control isDryActivated = control("isDryActivated");
    public final Control isPmdActivated = control("isPmdActivated");
    public final Control isOpenTasksActivated = control("isOpenTasksActivated");
    public final Control isWarningsActivated = control("isWarningsActivated");
    public final Control isFindbugsActivated = control("isFindBugsActivated");

    protected AnalysisCollectorPluginArea(PageObject parent, String path) {
        super(parent, path);
    }
}
