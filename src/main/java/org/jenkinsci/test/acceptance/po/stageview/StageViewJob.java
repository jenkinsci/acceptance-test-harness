package org.jenkinsci.test.acceptance.po.stageview;

import org.openqa.selenium.WebElement;

import java.util.Collection;
import java.util.List;

/**
 * Created by boris on 11.05.17.
 */
public class StageViewJob {

private WebElement webWebElement;
private List<StageViewStage> stageViewStages;
        String buildNo;
        String color;

public StageViewJob(WebElement webElement, List<StageViewStage> stageViewStages) {
        this.webWebElement = webElement;
        this.stageViewStages = stageViewStages;
        this.buildNo = webWebElement.getAttribute("data-runid");
        }

public Collection<StageViewStage> getAllStageViewItem() {
        return this.stageViewStages;
        }

public StageViewStage getStageViewItem(int buildNumber) {
        return this.stageViewStages.get(stageViewStages.size()-1);
        }

        @Override
        public String toString() {
        return this.buildNo + " - " + webWebElement.getText().replace("\n", "");
        }

}
