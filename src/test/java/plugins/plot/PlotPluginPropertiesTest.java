package plugins.plot;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.plot.*;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.JenkinsLogger;
import org.junit.Before;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;

import java.util.regex.Pattern;

import static org.junit.Assert.assertFalse;

@WithPlugins({
        "matrix-project", // JENKINS-37545
        "plot"
})
public class PlotPluginPropertiesTest extends AbstractJUnitTest {

    private FreeStyleJob job;
    private PlotPublisher pub;

    @Before
    public void setUp() {
        job = jenkins.jobs.create();
        pub = job.addPublisher(PlotPublisher.class);
    }

    @Test
    public void generate_simple_plot_properties(){
        job.configure();
        job.copyResource("/plot_plugin/plot.properties");

        Plot plot = pub.getPlot(1);
        plot.setGroup("Group_1");
        plot.setTitle("PropertiesPlot1");

        pub.source("properties", "plot.properties");
        job.save();
        job.startBuild().shouldSucceed();

        assertThatBuildHasPlot("PropertiesPlot1", "Group_1");

    }

    private void assertThatBuildHasPlot(String title, String group) {
        job.visit("plot");
        find(by.xpath("//h1[contains(text(), '%s')]", group));
        find(by.xpath("//select[@name='choice']/option[contains(text(), '%s')]",title));
    }
}
