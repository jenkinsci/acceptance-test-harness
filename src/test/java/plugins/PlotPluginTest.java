package plugins;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.plot.PlotPublisher;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.Job;
import org.junit.Test;

/**
 Feature: Adds Plotting support
   In order to be able to visualize build metrics
   As a Jenkins user
   I want to configure and generate various plots
 */
@WithPlugins("plot")
public class PlotPluginTest extends AbstractJUnitTest {
    /**
     Scenario: Generate simple plot
       Given I have installed the "plot" plugin
       And a job
       When I configure the job
       And I copy resource "plot_plugin/plot.csv" into workspace
       And I add plot "My plot" in group "My group"
       And I configure csv data source "plot.csv"
       And I save the job
       And I build the job
       Then the build should succeed
       And there should be a plot called "My plot" in group "My group"
     */
    @Test
    public void generate_simple_plot() {
        FreeStyleJob job = jenkins.jobs.create();
        job.configure();
        job.copyResource(resource("/plot_plugin/plot.csv"));
        PlotPublisher pub = job.addPublisher(PlotPublisher.class);
        pub.group.set("My group");
        pub.title.set("My plot");
        pub.source("csv","plot.csv");
        job.save();

        Build b = job.queueBuild().shouldSucceed();
        assertThatBuildHasPlot(job,"My plot","My group");
    }

    private void assertThatBuildHasPlot(Job job, String title, String group) {
        job.visit("plot");
        find(by.xpath("//h1[contains(text(), '%s')]", group));
        find(by.xpath("//select[@name='choice']/option[contains(text(), '%s')]",title));
    }
}
