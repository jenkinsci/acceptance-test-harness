package plugins;

import org.jenkinsci.test.acceptance.Matchers;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.xunit.XUnitPublisher;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.junit.Test;
import org.openqa.selenium.By;

import static org.hamcrest.MatcherAssert.assertThat;

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
        clickLink("Test Result");
        assertThat(driver, Matchers.hasContent("1 failures"));

        job.open();
        find(TEST_RESULT_TREND_CHART);
    }

    public static final By TEST_RESULT_TREND_CHART = by.css(".test-trend-caption");
}
