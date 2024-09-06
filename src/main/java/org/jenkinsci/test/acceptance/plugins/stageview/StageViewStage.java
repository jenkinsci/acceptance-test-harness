package org.jenkinsci.test.acceptance.plugins.stageview;

import org.openqa.selenium.WebElement;

/**
 * Single element of a job. The actual stage within a job.
 * @author Boris Dippolter
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
