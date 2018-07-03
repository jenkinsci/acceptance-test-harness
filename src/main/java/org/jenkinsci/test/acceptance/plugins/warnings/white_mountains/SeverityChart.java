package org.jenkinsci.test.acceptance.plugins.warnings.white_mountains;

import org.jenkinsci.test.acceptance.po.PageObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * Severity chart page object that provides the number of warnings per severity.
 *
 * @author Anna-Maria Hardi
 * @author Elvira Hauer
 */
public class SeverityChart {

    private WebElement priorityChart;

    /**
     * Constructor of the class {@link SeverityChart}.
     *
     * @param parent
     *         the parent PageObject
     */
    SeverityChart(final PageObject parent) {
        priorityChart = parent.getElement(By.id("number-priorities"));
    }

    /**
     * Getter for low priority.
     *
     * @return number of low priority
     */
    public int getLowPriority() {
        return Integer.parseInt(priorityChart.getAttribute("data-low"));
    }

    /**
     * Getter for normal priority.
     *
     * @return number of normal priority
     */
    public int getNormalPriority() {
        return Integer.parseInt(priorityChart.getAttribute("data-normal"));
    }

    /**
     * Getter for high priority.
     *
     * @return number of high priority
     */
    public int getHighPriority() {
        return Integer.parseInt(priorityChart.getAttribute("data-high"));
    }

}
