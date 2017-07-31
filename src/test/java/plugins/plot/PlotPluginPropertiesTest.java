package plugins.plot;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.Resource;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.plot.Plot;
import org.jenkinsci.test.acceptance.plugins.plot.PlotPublisher;
import org.jenkinsci.test.acceptance.plugins.plot.PropertiesDataSeries;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Properties;

@WithPlugins({
        "matrix-project", // JENKINS-37545
        "plot"
})
public class PlotPluginPropertiesTest extends AbstractJUnitTest {

    private FreeStyleJob job;

    private final String propertiesFilePath = "/plot_plugin/plot.properties";
    private final String propertiesFileName = "plot.properties";

    @Before
    public void setUp() {
        job = jenkins.jobs.create();

    }

    @Test
    public void generate_simple_plot_properties() {
        job.configure();
        job.copyResource(propertiesFilePath);
        PlotPublisher pub = job.addPublisher(PlotPublisher.class);

        Plot plot = pub.getPlot(1);
        plot.setGroup("Group_1");
        plot.setTitle("PropertiesPlot1");


        PropertiesDataSeries pSeries = plot.addDataSeries(PropertiesDataSeries.class, propertiesFileName);
        pSeries.setLabel("propLabel");

        job.save();
        job.startBuild().shouldSucceed();

        job.visit("plot");
        find(by.xpath("//h1[contains(text(), '%s')]", "Group_1"));
        find(by.xpath("//select[@name='choice']/option[contains(text(), '%s')]","PropertiesPlot1"));
    }

    @Test
    public void test_clickable_data_points() throws IOException {
        job.configure();
        job.copyResource(propertiesFilePath);
        PlotPublisher pub = job.addPublisher(PlotPublisher.class);

        final Resource res = resource(propertiesFilePath);
        Properties prop = new Properties();
        prop.load(res.asInputStream());

        Plot plot = pub.getPlot(1);
        plot.setGroup("Group_1");
        plot.setTitle("PropertiesPlot1");

        PropertiesDataSeries pSeries = plot.addDataSeries(PropertiesDataSeries.class, propertiesFileName);

        job.save();
        job.startBuild().shouldSucceed();

        job.visit("plot");

        find(by.xpath("//map/area[contains(@title, '%s')]", prop.getProperty("YVALUE")));
        find(by.xpath("//map/area[contains(@href, '%s')]", prop.getProperty("URL")));

    }



}
