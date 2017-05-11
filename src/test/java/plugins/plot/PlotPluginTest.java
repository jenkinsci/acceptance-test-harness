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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@WithPlugins({
        "matrix-project", // JENKINS-37545
        "plot"
})
public class PlotPluginTest extends AbstractJUnitTest {

    private FreeStyleJob job;
    private final String csvFilePath = "/plot_plugin/plot.csv";
    private final String csvFileName = "plot.csv";
    private final String csvWithStringsFilePath = "/plot_plugin/plot_with_strings.csv";
    private final String csvWithStringsFileName = "plot_with_strings.csv";
    private final String EMPTY_STRING = ".*java.lang.NumberFormatException: For input string: \"\".*";
    private final String NON_EMPTY_STRING = ".*java.lang.NumberFormatException: For input string: \".+\".*";

    @Before
    public void setUp() {
        job = jenkins.jobs.create();
    }


    @Test
    public void generate_simple_plot() {
        job.configure();
        job.copyResource(csvFilePath);

        PlotPublisher pub =job.addPublisher(PlotPublisher.class);
        Plot plot = pub.getPlot(1);
        plot.setGroup("My group");
        plot.setTitle("My plot");
        plot.setStyle("Line 3D");

        plot.addDataSeries(CsvDataSeries.class, csvFileName);
        job.save();

        job.startBuild().shouldSucceed();
        assertThatBuildHasPlot("My plot","My group");
    }

    @Test @Issue({"JENKINS-18585","JENKINS-18674"})
    public void postbuild_rendering_should_work() {
        job.configure();

        PlotPublisher pub =job.addPublisher(PlotPublisher.class);
        Plot plot = pub.getPlot(1);
        plot.setGroup("Plots");
        plot.setTitle("Some plot");
        job.save();

        job.configure();
        plot.addDataSeries(CsvDataSeries.class, csvFileName);
        job.save();

        job.startBuild().shouldSucceed();
        assertThatBuildHasPlot("Some plot","Plots");
    }


    /**
     * Test if NumberFormatException is logged only if a non-zero length string is found in the data set.
     */
    @Test
    @WithPlugins("plot@1.10") @Issue("JENKINS-25849")
    public void no_exception_visit_plot_page() {
        job.configure();
        job.copyResource(resource(csvWithStringsFilePath));

        PlotPublisher pub = job.addPublisher(PlotPublisher.class);
        Plot plot = pub.getPlot(1);
        plot.setGroup("My group");
        plot.setTitle("My plot");

        plot.addDataSeries(CsvDataSeries.class, csvWithStringsFileName);
        job.save();

        job.startBuild().shouldSucceed();
        job.visit("plot");

        assertThatBuildHasPlot("My plot", "My group");

        JenkinsLogger jLog = job.getJenkins().getLogger("all");

        Pattern p1 = Pattern.compile(EMPTY_STRING, Pattern.DOTALL);
        Pattern p2 = Pattern.compile(NON_EMPTY_STRING, Pattern.DOTALL);

        assertThat("NumberFormatException was logged for empty string.", jLog.hasLogged(p1), is(false));
        assertThat("NumberFormatException was not logged for non empty string.", jLog.hasLogged(p2), is(true));
    }

    private void assertThatBuildHasPlot(String title, String group) {
        job.visit("plot");
        find(by.xpath("//h1[contains(text(), '%s')]", group));
        find(by.xpath("//select[@name='choice']/option[contains(text(), '%s')]",title));
    }

}
