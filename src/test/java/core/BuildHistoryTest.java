package core;

import org.jenkinsci.test.acceptance.Matchers;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.junit.Test;
import org.openqa.selenium.WebDriver;

import javax.inject.Inject;

/**
 * Feature: Display build history
   As a Jenkins user or administrator
   I should be able to view the build history both globally or per-job
   So that I can identify build trends, times, etc.

 */
public class BuildHistoryTest extends AbstractJUnitTest {
    @Inject
    Jenkins j;

    @Inject
    WebDriver driver;

    /**
       Scenario: Viewing global build history
         Given a simple job
         When I build the job
         Then the global build history should show the build
     */
    @Test
    public void view_global_build_history() {
        FreeStyleJob job = j.createJob();
        job.queueBuild().waitUntilFinished();

        j.visit("view/All/builds");
        assertThat(driver, Matchers.hasContent(String.format("%s #1", job.name)));
    }
}
