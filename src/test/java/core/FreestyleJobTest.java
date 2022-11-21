package core;

import hudson.util.VersionNumber;
import org.apache.commons.io.IOUtils;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.SmokeTest;
import org.jenkinsci.test.acceptance.junit.Wait;
import org.jenkinsci.test.acceptance.po.Artifact;
import org.jenkinsci.test.acceptance.po.ArtifactArchiver;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.BuildWithParameters;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.Job;
import org.jenkinsci.test.acceptance.po.ListView;
import org.jenkinsci.test.acceptance.po.PasswordParameter;
import org.jenkinsci.test.acceptance.po.ShellBuildStep;
import org.jenkinsci.test.acceptance.po.StringParameter;
import org.jenkinsci.test.acceptance.po.TimerTrigger;
import org.jenkinsci.test.acceptance.po.UpstreamJobTrigger;
import org.jenkinsci.test.acceptance.utils.IOUtil;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.jvnet.hudson.test.Issue;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.jenkinsci.test.acceptance.Matchers.containsRegexp;
import static org.jenkinsci.test.acceptance.Matchers.containsString;
import static org.jenkinsci.test.acceptance.Matchers.hasContent;
import static org.jenkinsci.test.acceptance.Matchers.pageObjectDoesNotExist;
import static org.jenkinsci.test.acceptance.Matchers.pageObjectExists;
import static org.junit.Assert.assertTrue;

public class FreestyleJobTest extends AbstractJUnitTest {
    @Test
    public void should_use_upstream_trigger() {
        FreeStyleJob main = jenkins.jobs.create(FreeStyleJob.class);
        FreeStyleJob trigger = jenkins.jobs.create(FreeStyleJob.class);

        main.configure(() -> {
            UpstreamJobTrigger configuration = main.addTrigger(UpstreamJobTrigger.class);
            configuration.setUpstreamProjects(trigger.name);
        });

        Build build = trigger.scheduleBuild().shouldSucceed();
        assertThat(build.getConsole(), containsString("Triggering a new build of %s", main.name));

        Build automaticallyStartedBuild = main.build(1);
        automaticallyStartedBuild.waitUntilFinished();

        assertThat(automaticallyStartedBuild.getConsole(),
                containsString("Started by upstream project \"%s\"", trigger.name));
    }

    @Test
    public void should_set_description() {
        FreeStyleJob job = jenkins.jobs.create(FreeStyleJob.class);
        String description = "A description!";
        job.configure(() -> job.setDescription(description));

        WebElement actual = job.find(By.xpath("//div[@id='description']/div"));
        assertThat(actual.getText(), containsString(description));
    }

    @Test
    public void should_show_permalink_last_build() {
        FreeStyleJob job = jenkins.jobs.create(FreeStyleJob.class);

        String last = "Last build (#1)";
        assertThat(driver, not(hasContent(last)));

        job.scheduleBuild().waitUntilFinished();
        job.open();
        assertThat(driver, hasContent(last));
    }

    @Test
    public void should_visit_build_with_permalink() {
        FreeStyleJob job = jenkins.jobs.create(FreeStyleJob.class);

        Build build = job.scheduleBuild().shouldSucceed();
        job.open();
        WebElement link = job.find(By.partialLinkText("Last build (#1)"));
        String expectedUrl = link.getAttribute("href");
        
        Build b = new Build(job, "lastBuild");
        b.open();
        assertThat("Permalink link is current URL", driver.getCurrentUrl(), is(expectedUrl));
        assertThat("Build number is correct", b.getNumber(), is(1));
        assertThat("Build has no changes", driver, hasContent("No changes"));
        assertThat("Build is success", b.getResult(), is(Build.Result.SUCCESS.name()));
    }

    @Test @Issue("JENKINS-38928")
    public void apply_then_save() {
        FreeStyleJob j = jenkins.jobs.create(FreeStyleJob.class, "simple-job");
        assertThat(j, pageObjectExists());

        j.configure();
        ShellBuildStep shell = j.addBuildStep(ShellBuildStep.class);
        shell.command("echo 1");

        j.apply();
        j.save();

        String src = getConfigXml(j);
        assertThat(src, containsString("echo 1"));
    }

    @Test
    public void runCurrentBuilds() {
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
    public void disableJob() {
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
    @Category(SmokeTest.class)
    public void buildParametrized() {
        FreeStyleJob j = jenkins.jobs.create(FreeStyleJob.class);
        j.configure();
        j.addParameter(StringParameter.class).setName("text").setDefault("foo").setDescription("Bar");
        j.addParameter(PasswordParameter.class).setName("password").setDefault("foopass").setDescription("apass");
        j.addShellStep("echo \">$text<\"");
        j.save();

        Build build = j.scheduleBuild(Collections.singletonMap("text", "asdf")).waitUntilFinished();
        assertThat(build.getConsole(), containsString(">asdf<"));
    }

    @Test
    public void discardBuilds() {
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
        // TODO remove as soon as 'waitUntilFinished' effectively waiting for the build to be finished (including hudson.model.Job#logRotate)
        waitFor(b2).withMessage("waiting for build #2 to be deleted")
                .withTimeout(30, TimeUnit.SECONDS).until(pageObjectDoesNotExist());

        assertThat(b2, pageObjectDoesNotExist());
    }

    @Test
    @Category(SmokeTest.class)
    public void doNotDiscardSuccessfulBuilds() {
        FreeStyleJob j = jenkins.jobs.create(FreeStyleJob.class);

        j.configure();
        try {
            check("Discard old builds");
        } catch (NoSuchElementException x) { // 1.636-
            check("Discard Old Builds");
        }
        j.control(by.name("_.numToKeepStr")).set(1);
        ShellBuildStep shellBuildStep = j.addShellStep("exit 0");
        j.save();

        Build b1 = j.scheduleBuild().waitUntilFinished();

        j.configure();
        shellBuildStep.command("exit 1");
        j.save();

        Build b2 = j.scheduleBuild().waitUntilFinished();
        Build b3 = j.scheduleBuild().waitUntilFinished();
        assertThat(b1, pageObjectExists());
        assertThat(b2, pageObjectDoesNotExist());
        assertThat(b3, pageObjectExists());
    }

    @Test
    @Category(SmokeTest.class)
    public void archiveArtifacts() {
        FreeStyleJob j = jenkins.jobs.create(FreeStyleJob.class);
        j.configure();
        j.addShellStep("echo 'yes' > include; echo 'no' > exclude;");
        ArtifactArchiver archiver = j.addPublisher(ArtifactArchiver.class);
        archiver.includes("**/*include*");
        archiver.excludes("exclude");
        j.save();
        Build build = j.scheduleBuild().waitUntilFinished();
        assertThat(build.getArtifact("exclude"), pageObjectDoesNotExist());
        Artifact include = build.getArtifact("include");
        assertThat(include, pageObjectExists());
        assertThat(include.getTextContent(), equalTo("yes"));
    }

    @Test
    @Category(SmokeTest.class)
    public void buildPeriodically() {
        FreeStyleJob j = jenkins.jobs.create(FreeStyleJob.class);
        j.configure();
        TimerTrigger trigger = j.addTrigger(TimerTrigger.class);
        trigger.spec.set("* * * * *");
        j.save();

        Build first = j.build(1);
        new Wait<>(first)
                .withTimeout(70, TimeUnit.SECONDS) // Wall-clock time
                .until(pageObjectExists())
        ;
        assertThat(first.getConsole(), containsString("Started by timer"));

        assertThat(j.build(3), pageObjectDoesNotExist());
    }

    @Test
    public void customWorkspace() {
        FreeStyleJob j = jenkins.jobs.create(FreeStyleJob.class);
        j.configure();
        j.useCustomWorkspace("custom_workspace");
        j.save();
        Pattern expected = Pattern.compile("^Building in workspace (.*)custom_workspace$", Pattern.MULTILINE);
        assertThat(j.scheduleBuild().waitUntilFinished().getConsole(), containsRegexp(expected));
    }

    @Test
    public void scheduleFromView() throws Exception {
        FreeStyleJob j = jenkins.jobs.create(FreeStyleJob.class);
        ListView view = jenkins.views.create(ListView.class, "AView");
        view.configure();
        view.addJob(j);
        view.save();
        view.scheduleJob(j.name);
        j.build(1).waitUntilStarted().shouldSucceed();

        j.configure();
        StringParameter p = j.addParameter(StringParameter.class);
        p.setName("foo");
        j.save();

        view.scheduleJob(j.name);
        BuildWithParameters paramPage = new BuildWithParameters(j, new URL(driver.getCurrentUrl()));
        paramPage.enter(Collections.singletonList(p), Collections.singletonMap("foo", "bar"));
        paramPage.start();

        j.build(2).waitUntilStarted().shouldSucceed();
    }

    @Test @Issue({"JENKINS-21457", "JENKINS-20772", "JENKINS-21478"})
    public void showErrorSavingConfig() {
        FreeStyleJob j = jenkins.jobs.create(FreeStyleJob.class);
        j.configure();
        TimerTrigger trigger = j.addTrigger(TimerTrigger.class);
        trigger.spec.set("not_a_time");
        clickButton("Apply");

        String errorElementCSS = jenkins.getVersion().isOlderThan(new VersionNumber("2.235")) ? "#error-description pre" : ".validation-error-area .error";
        By error = by.css(errorElementCSS);
        
        assertThat(waitFor(error).getText(), containsString("Invalid input: \"not_a_time\""));
        clickLink("Close");

        j.configure();
        j.addTrigger(TimerTrigger.class);
        trigger.spec.set("not_a_time_either");
        clickButton("Apply");

        assertThat(waitFor(error).getText(), containsString("Invalid input: \"not_a_time_either\""));
        clickLink("Close");
    }

    @Test
    public void delete_a_simple_job() {
        FreeStyleJob j = jenkins.jobs.create(FreeStyleJob.class, "simple-job");
        assertThat(j, pageObjectExists());

        j.delete();

        elasticSleep(1000); // wait for delete to complete.
        assertThat(j,pageObjectDoesNotExist());
    }

    @Test
    public void copy_a_simple_job() {
        FreeStyleJob j = jenkins.jobs.create(FreeStyleJob.class, "simple-job");
        jenkins.jobs.copy(j, "simple-job-copy");
        assertThat(driver, hasContent("simple-job-copy"));

        FreeStyleJob k = jenkins.jobs.get(FreeStyleJob.class, "simple-job-copy");

        String jxml = getConfigXml(j);
        String kxml = getConfigXml(k);
        assertThat(jxml, is(kxml));
    }

    // Workaround for https://support.mozilla.org/en-US/questions/1193967
    private String getConfigXml(FreeStyleJob j) {
        try {
            HttpURLConnection con = IOUtil.openConnection(new URL(j.url, "config.xml"));
            return IOUtils.toString(con.getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new Error(e);
        }
    }
}
