package org.jenkinsci.test.acceptance.po.stageview;

import org.openqa.selenium.WebElement;

/**
 * Created by boris on 11.05.17.
 */
public class StageViewStage {

        private WebElement webElement;

        String name;
        String duration;
        String color;

        public StageViewStage(WebElement webElement) {
            this.webElement = webElement;
            this.name = webElement.getText().replace("\n", "");
        }
        @Override
        public String toString() {
            return "-- Stage: " + this.name;
        }
}
