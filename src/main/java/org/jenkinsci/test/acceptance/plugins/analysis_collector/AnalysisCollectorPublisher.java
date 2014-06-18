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

    private AnalysisCollectorPluginArea pluginArea;

    public AnalysisCollectorPublisher(Job parent, String path) {
        super(parent, path);
        this.pluginArea = new AnalysisCollectorPluginArea(parent, path);
    }

    /**
     * Select if the warnings of given plugin should be collected by Analysis Collector Plugin.
     *
     * @param plugin  the Plugin
     * @param checked true or false
     */
    public void checkCollectedPlugin(AnalysisPlugin plugin, boolean checked) {
        plugin.check(this.pluginArea, checked);
    }

}
