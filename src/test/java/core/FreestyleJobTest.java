package core;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.ShellBuildStep;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;

import static org.jenkinsci.test.acceptance.Matchers.pageObjectExists;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class FreestyleJobTest extends AbstractJUnitTest {

    @Test
    @Issue("JENKINS-38928")
    public void apply_then_save() {
        FreeStyleJob j = jenkins.jobs.create(FreeStyleJob.class, "simple-job");
        assertThat(j, pageObjectExists());

        j.configure();
        ShellBuildStep shell = j.addBuildStep(ShellBuildStep.class);
        shell.command("echo 1");

        j.apply();
        j.save();

        j.visit("config.xml");

        assertTrue("job config.xml should contain the step \"echo 1\"",driver.getPageSource().contains("echo 1"));
    }
}
