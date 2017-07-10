package org.jenkinsci.test.acceptance.plugins.analysis_core;

import org.jenkinsci.test.acceptance.po.ContainerPageObject;
import org.jenkinsci.test.acceptance.po.Control;
import org.openqa.selenium.WebDriver;

/**
 * Configuration screen of trend graphs.
 *
 * @author Ullrich Hafner
 */
public class GraphConfigurationView extends ContainerPageObject {
    private final Control width = control("/width");
    private final Control height = control("/height");
    private final Control buildCount = control("/buildCountString");
    private final Control dayCount = control("/dayCountString");
    private final Control parameterName = control("/parameterName");
    private final Control parameterValue = control("/parameterValue");
    private final Control useBuildDateAsDomain = control("/useBuildDateAsDomain");
    private final Control fixed = control("/graphType[FIXED]");
    private final Control priority = control("/graphType[PRIORITY]");
    private final Control totals = control("/graphType[TOTALS]");
    private final Control difference = control("/graphType[DIFFERENCE]");
    private final Control user = control("/graphType[USERS]");
    private final Control none = control("/graphType[NONE]");

    public GraphConfigurationView(final ContainerPageObject parent, final String plugin) {
        super(parent, parent.url("%s/configure/", plugin));
    }

    /**
     * Saves the trend graph configuration.
     */
    public void save() {
        clickButton("Save");
    }

    @Override
    public WebDriver open() {
        WebDriver open = super.open();

        elasticSleep(1000); // trend graphs take some time to compute...
        return open;
    }

    public void deactivateTrend() {
        none.click();
    }

    public void setUserGraph() {
        user.click();
    }
}
