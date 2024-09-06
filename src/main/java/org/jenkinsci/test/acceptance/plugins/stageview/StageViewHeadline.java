package org.jenkinsci.test.acceptance.plugins.stageview;

import org.openqa.selenium.WebElement;

/**
 * Representation of the headlines in the Stageview. The actual names of the stages.
 * @author Boris Dippolter
 */
public class StageViewHeadline {

    /**
     * Webelement locator for this current headline
     */
    private WebElement webElement;

    /**
     * The actual headline in the box. Sanitized.
     */
    private String name;

    /**
     * Constructor fot Headline
     *
     * @param webElement the parent element
     */
    public StageViewHeadline(WebElement webElement) {
        this.webElement = webElement;
        this.name = webElement.getText().replace("\n", "");
    }

    /**
     * Name of the headline
     * @return name
     */
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "-- Headline: " + this.name;
    }
}
