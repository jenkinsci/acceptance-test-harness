package org.jenkinsci.test.acceptance.plugins.stageview;

import java.util.List;
import org.openqa.selenium.WebElement;

/**
 * Representation of buildjobs done. Rows in the table.
 * @author Boris Dippolter
 */
public class StageViewBuild {

    /**
     * Webelement locator for this current headline
     */
    private WebElement webWebElement;

    /**
     * List of underlying stages
     */
    private List<StageViewStage> stageViewStages;

    /**
     * the current build number
     */
    private String buildNo;

    /**
     * String of css classes used for the build.
     */
    private String cssClasses;

    /**
     * Initializes a stageviewjob
     *
     * @param webElement      theElement of the particular job
     * @param stageViewStages The overall stages
     */
    public StageViewBuild(WebElement webElement, List<StageViewStage> stageViewStages) {
        this.webWebElement = webElement;
        this.stageViewStages = stageViewStages;
        this.buildNo = webWebElement.getAttribute("data-runid");
        this.cssClasses = webWebElement.getAttribute("class");
    }

    /**
     * Returns all the items of a particular job
     *
     * @return list of stageviewitems
     */
    public List<StageViewStage> getAllStageViewItem() {
        return this.stageViewStages;
    }

    /**
     * Return the stageViewItem
     *
     * @return specific item
     */
    public StageViewStage getStageViewItem(int stageNumber) {
        return this.stageViewStages.get(stageNumber);
    }

    /**
     * Return the current build no
     *
     * @return the number as  a String
     */
    public String getBuildNo() {
        return buildNo;
    }

    /**
     * Return the css classes as a String
     *
     * @return cssclasses
     */
    public String getCssClasses() {
        return cssClasses;
    }

    @Override
    public String toString() {
        return this.buildNo + " - " + webWebElement.getText().replace("\n", "") + " - css: " + this.cssClasses;
    }
}
