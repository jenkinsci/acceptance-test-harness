package org.jenkinsci.test.acceptance.plugins.analysis_collector;

import org.jenkinsci.test.acceptance.plugins.analysis_core.GraphConfigurationView;
import org.jenkinsci.test.acceptance.po.ContainerPageObject;
import org.jenkinsci.test.acceptance.po.Control;

/**
 * Configuration screen of trend graph for the analysis-collector plug-in.
 *
 * @author Ullrich Hafner
 */
public class AnalysisGraphConfigurationView extends GraphConfigurationView {
    private final Control canDeacticateOtherTrendGraphs = control("/canDeacticateOtherTrendGraphs");

    /**
     * Creates a new instance of {@link AnalysisGraphConfigurationView}.
     *
     * @param parent Parent container page object
     */
    public AnalysisGraphConfigurationView(final ContainerPageObject parent) {
        super(parent, "analysis");
    }

    /**
     * Deactivates or activates the other trend graphs.
     *
     * @param shouldDisable determines if the other trend graphs should be deactivated
     */
    public void deactiveOtherTrendGraphs(final boolean shouldDisable) {
        canDeacticateOtherTrendGraphs.check(shouldDisable);
    }
}
