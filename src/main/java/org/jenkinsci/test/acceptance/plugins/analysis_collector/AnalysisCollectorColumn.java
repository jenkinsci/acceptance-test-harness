package org.jenkinsci.test.acceptance.plugins.analysis_collector;

import org.jenkinsci.test.acceptance.po.AbstractListViewColumn;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.ListView;

/**
 * Column for Analysis Collector Plugin.
 *
 * @author Michael Prankl
 */
@Describable("Number of warnings")
public class AnalysisCollectorColumn extends AbstractListViewColumn {

    private AnalysisCollectorPluginArea pluginArea;

    public AnalysisCollectorColumn(ListView parent, String path) {
        super(parent, path);
        this.pluginArea = new AnalysisCollectorPluginArea(parent, path);
    }

    /**
     * Check plugins which should be included in warnings column.
     *
     * @param plugin  the plugin
     * @param checked true or false
     */
    public void checkPlugin(AnalysisPlugin plugin, boolean checked) {
        plugin.check(this.pluginArea, checked);
    }

}
