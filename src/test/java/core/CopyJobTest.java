package core;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.Parameter;
import org.jenkinsci.test.acceptance.po.StringParameter;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.jenkinsci.test.acceptance.Matchers.hasContent;
import static org.junit.Assert.assertTrue;

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

    @Test
    public void copy_a_simple_job_loads_build_params() {
        FreeStyleJob j = jenkins.jobs.create(FreeStyleJob.class, "simple-job");
        j.configure();
        j.addParameter(StringParameter.class).setName("Param1").setDefault("");
        j.addParameter(StringParameter.class).setName("Param2").setDefault("");
        assertTrue(Parameter.all().size() > 0);
        j.save();
        jenkins.jobs.copy(j, "simple-job-copy");
        assertThat(driver, hasContent("simple-job-copy"));

        FreeStyleJob k = jenkins.jobs.get(FreeStyleJob.class, "simple-job-copy");
        j.visit("config.xml");
        String jxml = driver.getPageSource();

        k.visit("config.xml");
        String kxml = driver.getPageSource();

        assertThat(jxml, is(kxml));
        assertTrue(k.getParameters().size() > 0);
        assertTrue(k.getParameters().size() == j.getParameters().size());
        assertTrue(k.getParameters().get(0).getName().equals(j.getParameters().get(0).getName()));
    }
}
