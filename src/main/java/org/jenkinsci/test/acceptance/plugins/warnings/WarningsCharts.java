package org.jenkinsci.test.acceptance.plugins.warnings;

import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.ContainerPageObject;
import org.jenkinsci.test.acceptance.po.Job;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class WarningsCharts extends ContainerPageObject {

    private static final String RESULT_PATH_END = "Result/";

    public WarningsCharts(final Job parent, final String id) {
        super(parent, parent.url(id.toLowerCase() + RESULT_PATH_END));
    }

    public WarningsCharts(final Build parent, final String id) {
        super(parent, parent.url(id.toLowerCase() + RESULT_PATH_END));
    }

    private WebElement getTrendChart() {
        return getElement(By.id("number-issues"));
    }

    public String getNewIssues() {
        WebElement chart = getTrendChart();
        return chart.getAttribute("data-new");
    }

    public String getFixedIssues() {
        WebElement chart = getTrendChart();
        return chart.getAttribute("data-fixed");
    }

    public String getOutstandingIssues() {
        WebElement chart = getTrendChart();
        return chart.getAttribute("data-outstanding");
    }

    private WebElement getPriorityChart() {
       return getElement(By.id("number-priorities"));
    }

    public String getLowPriority() {
        WebElement chart = getPriorityChart();
        return chart.getAttribute("data-low");
    }

    public String getNormalPriority() {
        WebElement chart = getPriorityChart();
        return chart.getAttribute("data-normal");
    }

    public String getHighPriority() {
        WebElement chart = getPriorityChart();
        return chart.getAttribute("data-high");
    }

}
