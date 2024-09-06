package org.jenkinsci.test.acceptance.plugins.dashboard_view.read;

import org.jenkinsci.test.acceptance.po.PageAreaImpl;
import org.jenkinsci.test.acceptance.po.PageObject;
import org.openqa.selenium.By;

/**
 * Provides a simple readonly area for the main panel.
 *
 * @author Peter Müller
 */
public class MainPanel extends PageAreaImpl {

    final By tabName = By.xpath("//div[@id=\"main-panel\"]//div[@class=\"tab active\"]/a");
    final By descriptionPath =
            By.xpath("//div[@id=\"main-panel\"]/div[@id=\"view-message\"]/div[@id=\"description\"]/div[1]");

    public MainPanel(PageObject context, String path) {
        super(context, path);
    }

    /**
     * Get the current active tab name.
     *
     * @return the name of the currently open tab
     */
    public String getTabName() {
        return find(tabName).getText();
    }

    /**
     * Get the description of the dashboard.
     *
     * @return the description of the dashboard
     */
    public String getDescription() {
        return find(descriptionPath).getText();
    }
}
