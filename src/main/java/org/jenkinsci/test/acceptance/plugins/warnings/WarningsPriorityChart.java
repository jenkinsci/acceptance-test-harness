package org.jenkinsci.test.acceptance.plugins.warnings;

import org.jenkinsci.test.acceptance.po.PageObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * A part of the PageObject {@link WarningsCharts}.
 *
 * @author Anna-Maria Hardi
 * @author Elvira Hauer
 */
public class WarningsPriorityChart {

    private WebElement priorityChart;

    /**
     * Constructor of the class {@link WarningsPriorityChart}.
     *
     * @param parent
     *         the parent PageObject
     */
    WarningsPriorityChart(final PageObject parent) {
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
