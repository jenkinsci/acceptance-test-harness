package core;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.jenkinsci.test.acceptance.Matchers.hasContent;

public class CopyJobTest extends AbstractJUnitTest {
    @Test
    public void copy_a_simple_job() {
        FreeStyleJob j = jenkins.jobs.create(FreeStyleJob.class, "simple-job");
        jenkins.jobs.copy(j, "simple-job-copy");
        assertThat(driver, hasContent("simple-job-copy"));

        FreeStyleJob k = jenkins.jobs.get(FreeStyleJob.class, "simple-job-copy");

        j.visit("config.xml");
        String jxml = driver.getPageSource();

        k.visit("config.xml");
        String kxml = driver.getPageSource();

        assertThat(jxml, is(kxml));
    }
}
