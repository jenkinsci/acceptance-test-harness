package core;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.Job;
import org.jenkinsci.test.acceptance.po.ShellBuildStep;
import org.jenkinsci.test.acceptance.po.StringParameter;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;
import org.openqa.selenium.NoSuchElementException;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.jenkinsci.test.acceptance.Matchers.pageObjectDoesNotExist;
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

    @Test
    public void runCurrentBuilds() throws Exception {
        FreeStyleJob j = jenkins.jobs.create(FreeStyleJob.class);
        j.configure();
        j.concurrentBuild.check();
        j.addShellStep("sleep 20");
        j.save();
        Build b1 = j.scheduleBuild().waitUntilStarted();
        Build b2 = j.scheduleBuild().waitUntilStarted();

        assertTrue(b1.isInProgress());
        assertTrue(b2.isInProgress());
    }

    @Test
    public void disableJob() throws Exception {
        FreeStyleJob j = jenkins.jobs.create(FreeStyleJob.class);
        assertThat(driver, not(Job.disabled()));

        j.configure();
        j.disable();
        j.save();

        assertThat(driver, Job.disabled());

        clickButton("Enable");

        assertThat(driver, not(Job.disabled()));
    }

    @Test
    public void buildParameterized() throws Exception {
        FreeStyleJob j = jenkins.jobs.create(FreeStyleJob.class);
        j.configure();
        j.addParameter(StringParameter.class).setName("text").setDefault("foo").setDescription("Bar");
        j.addShellStep("echo \">$text<\"");
        j.save();

        Build build = j.scheduleBuild(Collections.singletonMap("text", "asdf")).waitUntilFinished();
        assertThat(build.getConsole(), containsString(">asdf<"));
    }

    @Test
    public void discardBuilds() throws Exception {
        FreeStyleJob j = jenkins.jobs.create(FreeStyleJob.class);

        j.configure();
        try {
            check("Discard old builds");
        } catch (NoSuchElementException x) { // 1.636-
            check("Discard Old Builds");
        }
        j.control(by.name("_.numToKeepStr")).set(1);
        j.save();

        Build b1 = j.scheduleBuild().waitUntilFinished();
        Build b2 = j.scheduleBuild().waitUntilFinished();
        assertThat(b1, pageObjectDoesNotExist());
        assertThat(b2, pageObjectExists());

        b2.keepForever(true);

        Build b3 = j.scheduleBuild().waitUntilFinished();
        assertThat(b2, pageObjectExists());
        assertThat(b3, pageObjectExists());

        Build b4 = j.scheduleBuild().waitUntilFinished();
        assertThat(b2, pageObjectExists());
        assertThat(b3, pageObjectDoesNotExist());
        assertThat(b4, pageObjectExists());

        b2.keepForever(false);

        j.scheduleBuild().waitUntilFinished();
        assertThat(b2, pageObjectDoesNotExist());
    }
}
