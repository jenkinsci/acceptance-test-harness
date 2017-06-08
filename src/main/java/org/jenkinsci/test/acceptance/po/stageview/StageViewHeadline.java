package org.jenkinsci.test.acceptance.po.stageview;

import org.openqa.selenium.WebElement;

/**
 * Created by boris on 11.05.17.
 */
public class StageViewHeadline {

    private WebElement webElement;
    private String name;

    /**
     * Constructor fot Headline
     * @param webElement the parent element
     */
    public StageViewHeadline(WebElement webElement) {
        this.webElement = webElement;
        this.name = webElement.getText().replace("\n", "");
    }

    @Override
    public String toString() {
        return "-- Headline: " + this.name;
    }
}