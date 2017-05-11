package plugins.plot;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.plot.Plot;
import org.jenkinsci.test.acceptance.plugins.plot.PlotPublisher;
import org.jenkinsci.test.acceptance.plugins.plot.XmlDataSeries;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

@WithPlugins({
        "matrix-project", // JENKINS-37545
        "plot"
})
public class PlotPluginXmlTest extends AbstractJUnitTest {

    private FreeStyleJob job;

    @Before
    public void setUp() {
        job = jenkins.jobs.create();
    }

    @Test
    public void generateSimpleXmlTest() {
        setupFirstPlot();

        job.save();
        job.startBuild().shouldSucceed();
        job.visit("plot");

        find(by.xpath("//map/area[contains(@href, '%s')]", "http://foo.bar"));

    }

    @Ignore("Plot-Plugin functionality not yet ready")
    @Test
    public void generateXmlPlotWithTwoDataSeriesTest() {
        Plot p1 = setupFirstPlot();

        // this will override first csv line inside plot
        XmlDataSeries xmlDataSeries2 = p1.addDataSeries(XmlDataSeries.class);
        xmlDataSeries2.setFile("plot.xml");
        xmlDataSeries2.setUrl("http://foo.foo");
        xmlDataSeries2.setXpath("count(/books/book[author='Max, Mustermann'])");
        xmlDataSeries2.selectResultTypNumber();

        job.save();
        job.startBuild().shouldSucceed();
        job.visit("plot");

        find(by.xpath("//map/area[contains(@href, '%s')]", "http://foo.foo"));
        find(by.xpath("//map/area[contains(@href, '%s')]", "http://foo.bar"));

    }

    private Plot setupFirstPlot() {
        job.configure();
        job.copyResource(resource("/plot_plugin/plot.xml"));
        PlotPublisher pub = job.addPublisher(PlotPublisher.class);

        Plot p1 = pub.getPlot(1);
        p1.setGroup("G1");
        p1.setTitle("XML plot");
        p1.setStyle("Line");

        XmlDataSeries xmlDataSeries1 =  p1.addDataSeries(XmlDataSeries.class);
        xmlDataSeries1.setFile("plot.xml");
        xmlDataSeries1.setXpath("count(/books/book[price>35.00])");
        xmlDataSeries1.setUrl("http://foo.bar");
        xmlDataSeries1.selectResultTypNumber();

        return p1;
    }

}
