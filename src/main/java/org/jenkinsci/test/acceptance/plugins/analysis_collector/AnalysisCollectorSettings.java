package org.jenkinsci.test.acceptance.plugins.analysis_collector;

import org.jenkinsci.test.acceptance.plugins.analysis_core.AnalysisFreestyleSettings;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.Job;

/**
 * @author Michael Prankl
 */
@Describable("Publish combined analysis results")
public class AnalysisCollectorSettings extends AnalysisFreestyleSettings {

    private AnalysisCollectorPluginArea pluginArea;

    /**
     * Constructor for the build settings page area.
     *
     * @param parent       the job currently being configured.
     * @param selectorPath the selector path used as prefix.
     */
    public AnalysisCollectorSettings(Job parent, String selectorPath) {
        super(parent, selectorPath);
        this.pluginArea = new AnalysisCollectorPluginArea(parent, selectorPath);
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
