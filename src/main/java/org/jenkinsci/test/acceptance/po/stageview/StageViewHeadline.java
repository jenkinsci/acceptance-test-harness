package org.jenkinsci.test.acceptance.po.stageview;

import org.openqa.selenium.WebElement;

/**
 * Created by boris on 11.05.17.
 */
public class StageViewHeadline {

    private WebElement webElement;

    public StageViewHeadline(WebElement webElement) {
        this.webElement = webElement;
        this.name = webElement.getText().replace("\n", "");
    }

    String name;
    String duration;
    String color;

    @Override
    public String toString() {
        return "-- Haedline: " + this.name;
    }
}