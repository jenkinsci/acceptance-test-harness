package org.jenkinsci.test.acceptance.plugins.warnings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.ContainerPageObject;
import org.jenkinsci.test.acceptance.po.Job;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class WarningsTrendChart extends ContainerPageObject {

    private static final String RESULT_PATH_END = "Result/";

    public WarningsTrendChart(final Job parent, final String id) {
        super(parent, parent.url(id.toLowerCase() + RESULT_PATH_END));
    }

    public WarningsTrendChart(final Build parent, final String id) {
        super(parent, parent.url(id.toLowerCase() + RESULT_PATH_END));
    }

    private WebElement getTrendChart() {
        return getElement(By.id("number-issues"));
    }

//    private WebElement getPriorityChart() {
//        return getElement(By.id("TODO"));
//    }

    public WebElement getNewIssues() {
        return getElement(By.id("number-issues"));
    }

    public WebElement getFixedIssues() {
        return getElement(By.name("data_fixed"));
    }

    public WebElement getOutstandingIssues() {
        return getElement(By.name("data_outstanding"));
    }

    public WebElement getLowPriority() {
        return getElement(By.name("data_low"));
    }

    public WebElement getNormalPriority() {
        return getElement(By.name("data_normal"));
    }

    public WebElement getHighPriority() {
        return getElement(By.name("data_high"));
    }


}
