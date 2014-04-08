package plugins;

import org.jenkinsci.test.acceptance.Matchers;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
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

    /**
     Scenario: Publish xUnit results
       Given I have installed the "xunit" plugin
       And a job
       When I configure the job
       And I copy resource "junit/failure" into workspace
       And I publish "JUnit" report from "failure/TEST*.xml"
       And I save the job
       And I build 2 jobs
       Then the build should succeed
       And I visit build action named "Test Result"
       Then the page should say "1 failures"
       And the job page should contain test result trend chart
     */
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

        job.queueBuild().shouldSucceed();
        Build b = job.queueBuild().shouldSucceed();

        b.open();
        clickLink("Test Result");
        assertThat(driver, Matchers.hasContent("1 failures"));

        job.open();
        find(TEST_RESULT_TREND_CHART);
    }

    public static final By TEST_RESULT_TREND_CHART = by.xpath("//img[@alt='[Test result trend chart]']");
}
