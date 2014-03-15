package core;

import org.jenkinsci.test.acceptance.Matchers;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.junit.Test;

/**
 * Feature: Display build history
   As a Jenkins user or administrator
   I should be able to view the build history both globally or per-job
   So that I can identify build trends, times, etc.

 */
public class BuildHistoryTest extends AbstractJUnitTest {
    /**
       Scenario: Viewing global build history
         Given a simple job
         When I build the job
         Then the global build history should show the build
     */
    @Test
    public void view_global_build_history() {
        FreeStyleJob job = jenkins.jobs.create();
        job.queueBuild().waitUntilFinished();

        jenkins.visit("view/All/builds");
        assertThat(driver, Matchers.hasContent(String.format("%s #1", job.name)));
    }
}
