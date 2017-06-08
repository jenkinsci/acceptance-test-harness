package org.jenkinsci.test.acceptance.po.stageview;

import org.openqa.selenium.WebElement;

/**
 * Created by boris on 11.05.17.
 */
public class StageViewStage {

    /**
     * Webelement locator for this current headline
     */
    private WebElement webElement;

    /**
     * full naem of the current stage
     */
    private String name;

    public StageViewStage(WebElement webElement) {
        this.webElement = webElement;
        this.name = webElement.getText().replace("\n", "");
    }

    @Override
    public String toString() {
        return "-- Stage: " + this.name;
    }
}
