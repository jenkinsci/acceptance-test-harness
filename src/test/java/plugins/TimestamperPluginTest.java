package plugins;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.timestamper.TimstamperGlobalConfig;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebElement;

import java.util.List;

import static org.jenkinsci.test.acceptance.Matchers.*;

/**
 Feature: Timestamper support
   In order to have more informative build logs
   As a Jenkins user
   I want to decorate console log entries with timestamps
 */
@WithPlugins("timestamper")
public class TimestamperPluginTest extends AbstractJUnitTest {

    private FreeStyleJob job;

    /**
     Background:
       Given I have installed the "timestamper" plugin
       And a job inserting timestamps
     */
    @Before
    public void setUp() {
        job = jenkins.jobs.create();
        job.configure();
        job.find(by.path("/hudson-plugins-timestamper-TimestamperBuildWrapper")).click();
        job.save();
    }

    private void setTimestamp(String mode) {
        visit(job.getLastBuild().getConsoleUrl());
        driver.findElement(by.radioButton(mode)).click();
    }

    private List<WebElement> timestamps() {
        return all(by.xpath("//pre/span[@class='timestamp']"));
    }

    private void assertThatTimeStampMatchesRegexp(String regexp) {
        for (WebElement e : timestamps()) {
            assertThat(e.getText().trim(), containsRegexp(regexp));
        }
    }

    /**
     Scenario: Display no timestamps
       When I build the job
       And I select no timestamps
       Then there are no timestamps in the console
     */
    @Test
    public void display_no_timestamp() {
        job.queueBuild().waitUntilFinished();
        setTimestamp("timestamper-none");
        assertThatTimeStampMatchesRegexp("^$");
    }

    /**
     Scenario: Display system time timestamps
       When I build the job
       And I select system time timestamps
       Then console timestamps matches regexp "\d\d:\d\d:\d\d"
     */
    @Test
    public void display_system_time_timestamp() {
        job.queueBuild().waitUntilFinished();
        setTimestamp("timestamper-systemTime");
        assertThatTimeStampMatchesRegexp("^\\d\\d:\\d\\d:\\d\\d$");
    }

    /**
     Scenario: Display elapsed time timestamps
       When I build the job
       And I select elapsed time timestamps
       Then console timestamps matches regexp "\d\d:\d\d:\d\d.\d\d\d"
     */
    @Test
    public void display_elapsed_time_timestamp() {
        job.queueBuild().waitUntilFinished();
        setTimestamp("timestamper-elapsedTime");
        assertThatTimeStampMatchesRegexp("^\\d\\d:\\d\\d:\\d\\d.\\d\\d\\d$");
    }

    /**
     Scenario: Display specific system time timestamps
       When I set "'At 'HH:mm:ss' system time'" as system time timestamp format
       And I build the job
       And I select system time timestamps
       Then console timestamps matches regexp "At \d\d:\d\d:\d\d system time"
     */
    @Test
    public void display_specific_system_time_timestamps() {
        jenkins.configure();
        new TimstamperGlobalConfig(jenkins).systemTimeFormat.set("'At 'HH:mm:ss' system time'");
        jenkins.save();
        job.queueBuild().waitUntilFinished();

        setTimestamp("timestamper-systemTime");
        assertThatTimeStampMatchesRegexp("^At \\d\\d:\\d\\d:\\d\\d system time$");
    }

    /**
     Scenario: Display specific elapsed time timestamps
       When I set "'Exactly 'HH:mm:ss.S' after launch'" as elapsed time timestamp format
       And I build the job
       And I select elapsed time timestamps
       Then console timestamps matches regexp "Exactly \d\d:\d\d:\d\d.\d\d\d after launch"
     */
    @Test
    public void display_specific_elapsed_time_timestamps() {
        jenkins.configure();
        new TimstamperGlobalConfig(jenkins).elapsedTimeFormat.set("'Exactly 'HH:mm:ss.S' after launch");
        jenkins.save();
        job.queueBuild().waitUntilFinished();

        setTimestamp("timestamper-elapsedTime");
        assertThatTimeStampMatchesRegexp("^Exactly \\d\\d:\\d\\d:\\d\\d.\\d\\d\\d after launch$");
    }
}
