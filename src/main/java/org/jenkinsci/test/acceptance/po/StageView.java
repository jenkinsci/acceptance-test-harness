package org.jenkinsci.test.acceptance.po;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by boris on 26.04.17.
 */
public class StageView {

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


