package core;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.jenkinsci.test.acceptance.Matchers.*;

/**
 Feature: Copy a job
   Copy a job and check if exists and has the same configuration as the original job
 */
public class CopyJobTest extends AbstractJUnitTest {
    /**
     Scenario: Copy a simple job
       When I create a job named "simple-job"
       And I copy the job named "simple-job-copy" from job named "simple-job"
       Then the page should say "simple-job-copy"
       And the job configuration should be equal to "simple-job" configuration
     */
    @Test
    public void copy_a_simple_job() {
        FreeStyleJob j = jenkins.createJob(FreeStyleJob.class, "simple-job");
        jenkins.copyJob(j, "simple-job-copy");
        assertThat(driver, hasContent("simple-job-copy"));

        FreeStyleJob k = jenkins.getJob(FreeStyleJob.class, "simple-job-copy");

        j.visit("config.xml");
        String jxml = driver.getPageSource();

        k.visit("config.xml");
        String kxml = driver.getPageSource();

        assertThat(jxml, is(kxml));
    }
}
