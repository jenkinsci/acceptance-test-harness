package org.jenkinsci.test.acceptance.po;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by boris on 26.04.17.
 */
public class StageView extends  PageAreaImpl {

    /**
     * Main bocx for all content
     */
    public static final String ID_WRAPPER = "pipeline-box";

    /**
     * Main job wrapper. Alle children in this are "tr".
     */
    public static final String XPATH_JOBS = "//*[@id=\"pipeline-box\"]/div/div/table/tbody[2]";

    //public static final String XPATh_Exmaple_for stages in job
    // = //*[@id="pipeline-box"]/div/div/table/tbody[2]/tr[1]/td[2]

    public StageView(PageObject context, String path) {
        super(context, path);
    }

    public Collection getAllStageViewJobs() {
        //TODO implement
        return new ArrayList();
    }

    public StageViewJob getLatestBuild() {
        //TODO implement
        return null;
    }

    public StageViewJob getFirstBuild() {
        //TODO implement
        return null;
    }

    public StageView getBuildByBuildNumber(int buildNumber) {
        //TODO  implement
        return null;
    }

    public Collection getAllStageViewHeadLines() {
        //TODO implement
        return null;
    }

    public WebElement getRootElementName() {
        return super.find(By.id("pipeline-box"));
    }

}

class StageViewJob {

    String buildNo;
    String color;

    public Collection getAllStageViewStages() {
        //TODO implement
        return null;
    }

    public StageViewItem getStageViewItem(int buildNumber) {
        //TODO implement
        return null;
    }

}

class StageViewHeadLine{
    String name;
    String duration;
    String color;
}

class StageViewItem {
    String name;
    String duration;
    String color;

}


