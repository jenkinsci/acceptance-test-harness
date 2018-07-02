package org.jenkinsci.test.acceptance.plugins.warnings.white_mountains;

import org.jenkinsci.test.acceptance.po.PageObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * A part of the PageObject {@link WarningsCharts}.
 *
 * @author Anna-Maria Hardi
 * @author Elvira Hauer
 */
public class WarningsTrendChart {

    private WebElement trendChart;

    /**
     * Constructor of the class {@link WarningsTrendChart}.
     *
     * @param parent
     *         the parent PageObject
     */
    WarningsTrendChart(final PageObject parent) {
        trendChart = parent.getElement(By.id("number-issues"));
    }

    /**
     * Getter for new issues.
     *
     * @return number of new issues
     */
    public int getNewIssues() {
        return Integer.parseInt(trendChart.getAttribute("data-new"));
    }

    /**
     * Getter for fixed issues.
     *
     * @return number of fixed issues
     */
    public int getFixedIssues() {
        return Integer.parseInt(trendChart.getAttribute("data-fixed"));
    }

    /**
     * Getter for outstanding issues.
     *
     * @return number of outstanding issues
     */
    public int getOutstandingIssues() {
        return Integer.parseInt(trendChart.getAttribute("data-outstanding"));
    }
}
