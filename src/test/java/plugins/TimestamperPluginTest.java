package plugins;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.jenkinsci.test.acceptance.Matchers.containsRegexp;

import java.util.List;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.timestamper.TimstamperGlobalConfig;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebElement;

@WithPlugins("timestamper")
public class TimestamperPluginTest extends AbstractJUnitTest {

    private FreeStyleJob job;

    @Before
    public void setUp() {
        job = jenkins.jobs.create();
        job.configure();
        job.find(by.checkbox("Add timestamps to the Console Output")).click();
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

    @Test
    public void display() {
        job.startBuild().waitUntilFinished();

        setTimestamp("None");
        assertThatTimeStampMatchesRegexp("^$");

        setTimestamp("System clock time");
        assertThatTimeStampMatchesRegexp("^\\d\\d:\\d\\d:\\d\\d$");

        setTimestamp("Elapsed time");
        assertThatTimeStampMatchesRegexp("^\\d\\d:\\d\\d:\\d\\d.\\d\\d\\d$");
    }

    @Test
    public void display_specific_timestamps() {
        jenkins.configure();
        new TimstamperGlobalConfig(jenkins).systemTimeFormat.set("'At 'HH:mm:ss' system time'");
        new TimstamperGlobalConfig(jenkins).elapsedTimeFormat.set("'Exactly 'HH:mm:ss.S' after launch");
        jenkins.save();
        job.startBuild().waitUntilFinished();

        setTimestamp("System clock time");
        assertThatTimeStampMatchesRegexp("^At \\d\\d:\\d\\d:\\d\\d system time$");

        setTimestamp("Elapsed time");
        assertThatTimeStampMatchesRegexp("^Exactly \\d\\d:\\d\\d:\\d\\d.\\d\\d\\d after launch$");
    }
}
