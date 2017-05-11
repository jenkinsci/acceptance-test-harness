package org.jenkinsci.test.acceptance.po.stageview;

import org.jenkinsci.test.acceptance.po.PageAreaImpl;
import org.jenkinsci.test.acceptance.po.PageObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by boris on 26.04.17.
 */
//public class StageView {
    public class StageView extends PageAreaImpl {

    /**
     * Main bocx for all content
     */
    public static final String ID_WRAPPER = "pipeline-box";

    /**
     * Main job wrapper. Alle children in this are "tr".
     */
    public static final String XPATH_JOBS = "//*[@id=\"pipeline-box\"]/div/div/table/tbody[2]";


    //public WebDriver driver;

    public List<StageViewHeadline> stageViewHeadlines = new ArrayList<>();
    public List<StageViewJob> jobs = new ArrayList<>();

    public String rootElementName;
    //public static final String XPATh_Exmaple_for stages in job
    // = //*[@id="pipeline-box"]/div/div/table/tbody[2]/tr[1]/td[2]

    public StageView(PageObject context, String path) {
        super(context, path);
        this.buildStructure();
    }
    //public StageView(WebDriver driver) {
    //    this.driver = driver;
    //    this.buildStructure();
    //}

    public void buildStructure() {
        List<WebElement> children = driver.findElements(By.xpath("//*[@id=\"pipeline-box\"]/div/div/table/tbody[2]/tr"));

        for (WebElement e: children) {

            ArrayList<StageViewStage> stages = new ArrayList<>();

            for (WebElement f : e.findElements(By.className("stage-cell"))) {
                StageViewStage stage = new StageViewStage(f);
                stages.add(stage);
            }
            StageViewJob stageViewJob = new StageViewJob(e,stages);
            jobs.add(stageViewJob);
        }

        List<WebElement> headLines = driver.findElements(By.xpath("//*[@id=\"pipeline-box\"]/div/div/table/thead/tr/th"));
        for(WebElement webElement : headLines) {
            if(!webElement.getText().isEmpty()) {
                this.stageViewHeadlines.add(new StageViewHeadline(webElement));
            }
        }

        this.rootElementName = driver.findElement(By.id("pipeline-box")).getText();

        System.out.println("------------------------------------------");

        for (StageViewHeadline stageViewHeadline : this.stageViewHeadlines) {
            System.out.println(stageViewHeadline);
        }

        for (StageViewJob job: jobs) {
            System.out.println(job);
            for(StageViewStage stageViewStage: job.getAllStageViewItem()) {
                System.out.println(stageViewStage);
            }
        }
    }



    public List<StageViewJob> getAllStageViewJobs() {
        return this.jobs;
    }



    public StageViewJob getLatestBuild() {
        return this.jobs.get(0);
    }

    public StageViewJob getFirstBuild() {
        return this.jobs.get(jobs.size()-1);
    }

    public StageViewJob getBuildByBuildNumber(int buildNumber) {
        return this.jobs.get(buildNumber);
    }

    public List<StageViewHeadline> getStageViewHeadlines() {
        return this.stageViewHeadlines;
    }

    public WebElement getRootElementName() {
        return super.find(By.id("pipeline-box"));
    }

}