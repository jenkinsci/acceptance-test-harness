package org.jenkinsci.test.acceptance.plugins.stageview;

import org.jenkinsci.test.acceptance.po.PageAreaImpl;
import org.jenkinsci.test.acceptance.po.PageObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic container for the stageview implementation. The stageview is the graphical
 * representation of the pipeline builds. In the current abstraction the stagview contains
 * headlines and jobs to build the matrix of the build history.
 * The stageview is located on the jobs page right above the navigation links.
 * @author Boris Dippolter
 */
public class StageView extends PageAreaImpl {

    private static final Logger LOG = LoggerFactory.getLogger(StageView.class);

    /**
     * Main box for all content
     */
    private static final String ID_WRAPPER = "pipeline-box";

    /**
     * Main job wrapper. Alle children in this are "tr".
     */
    private static final String XPATH_JOBS = "//*[@id=\"pipeline-box\"]/div/div/table/tbody[2]";

    /**
     * Table heading xpath
     */
    private static final String XPATH_JOB_HEADLINES = "//*[@id=\"pipeline-box\"]/div/div/table/thead/tr/th";

    /**
     * Headlines are represented by the name of the stages
     */
    private List<StageViewHeadline> stageViewHeadlines = new ArrayList<>();

    /**
     * A StageviewJob represents the build
     */
    private List<StageViewBuild> jobs = new ArrayList<>();

    /**
     * root Element
     */
    public String rootElementName;

    public StageView(PageObject context, String path) {
        super(context, path);
        this.buildStructure();
    }

    /**
     * To generate and build up the whole stageview structure.
     */
    public void buildStructure() {
        List<WebElement> children = driver.findElements(By.xpath(XPATH_JOBS + "/tr"));

        for (WebElement e : children) {

            ArrayList<StageViewStage> stages = new ArrayList<>();

            for (WebElement f : e.findElements(By.className("stage-cell"))) {
                StageViewStage stage = new StageViewStage(f);
                stages.add(stage);
            }
            StageViewBuild stageViewBuild = new StageViewBuild(e, stages);
            jobs.add(stageViewBuild);
        }

        List<WebElement> headLines = driver.findElements(By.xpath(XPATH_JOB_HEADLINES));
        for (WebElement webElement : headLines) {
            if (!webElement.getText().isEmpty()) {
                this.stageViewHeadlines.add(new StageViewHeadline(webElement));
            }
        }

        this.rootElementName = driver.findElement(By.id("pipeline-box")).getText();

        for (StageViewHeadline stageViewHeadline : this.stageViewHeadlines) {
            LOG.debug("StageviewHeadline {} ", stageViewHeadline);
        }

        for (StageViewBuild job : jobs) {
            System.out.println(job);
            for (StageViewStage stageViewStage : job.getAllStageViewItem()) {
                LOG.debug("StageviewHeadline {} ", stageViewStage);
            }
        }
    }

    /**
     * Returns all builds
     *
     * @return All stageview Jobs
     */
    public List<StageViewBuild> getAllStageViewJobs() {
        return this.jobs;
    }

    /**
     * Returns first build
     *
     * @return All stageview Jobs
     */
    public StageViewBuild getFirstBuild() {
        return this.jobs.get(0);
    }

    /**
     * Returns the latest build
     *
     * @return stageview Job
     */
    public StageViewBuild getLatestBuild() {
        return this.jobs.get(jobs.size() - 1);
    }

    /**
     * Returns a stageviewJob wiht a specific bild number
     *
     * @return stageview Job
     */
    public StageViewBuild getBuildByBuildNumber(int buildNumber) {
        return this.jobs.get(buildNumber);
    }

    /**
     * Returns all headlines which are specified in the pipeline
     *
     * @return list of all headlines
     */
    public List<StageViewHeadline> getStageViewHeadlines() {
        return this.stageViewHeadlines;
    }

    /**
     * Returns the root eelement
     *
     * @return the root element
     */
    public WebElement getRootElementName() {
        return super.find(By.id("pipeline-box"));
    }

}
