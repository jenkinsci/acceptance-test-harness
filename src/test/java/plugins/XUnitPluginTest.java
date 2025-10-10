package plugins;

import static org.junit.Assert.assertEquals;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.junit.TestReport;
import org.jenkinsci.test.acceptance.plugins.xunit.XUnitPublisher;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.junit.Test;
import org.openqa.selenium.By;

/**
 * @author Kohsuke Kawaguchi
 */
@WithPlugins("xunit")
public class XUnitPluginTest extends AbstractJUnitTest {

    @Test
    public void publish_xunit_results() {
        FreeStyleJob job = jenkins.jobs.create();
        job.configure();
        {
            job.copyResource(resource("/junit/failure/com.simple.project.AppTest.txt"));
            job.copyResource(resource("/junit/failure/TEST-com.simple.project.AppTest.xml"));
            XUnitPublisher p = job.addPublisher(XUnitPublisher.class);
            p.addTool("JUnit").pattern.set("TEST*.xml");
        }
        job.save();

        job.startBuild().shouldSucceed();
        Build b = job.startBuild().shouldSucceed();

        b.open();
        TestReport testReport = b.action(TestReport.class).openViaLink();
        assertEquals("There should be 1 failing tests", testReport.getFailedTestCount(), 1);

        job.open();
        find(TEST_RESULT_TREND_CHART);
    }

    public static final By TEST_RESULT_TREND_CHART = by.css(".test-trend-caption");
}
