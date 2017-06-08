package org.jenkinsci.test.acceptance.po.stageview;

import org.openqa.selenium.WebElement;

/**
 * Created by boris on 11.05.17.
 */
public class StageViewStage {

    /**
     * Web element locator for this current headline
     */
    private WebElement webElement;

    /**
     * full name of the current stage
     */
    private String name;

    public StageViewStage(WebElement webElement) {
        this.webElement = webElement;
        this.name = webElement.getText().replace("\n", "");
    }

    /**
     * Name of the stage
     * @return name as String
     */
    public String getName() {
        return name;
    }


    @Override
    public String toString() {
        return "-- Stage: " + this.name;
    }
}
