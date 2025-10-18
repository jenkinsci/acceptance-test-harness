package plugins;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.junit.TestReport;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.JUnitPublisher;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;

/**
 * @author Kohsuke Kawaguchi
 */
@WithPlugins("junit")
public class JUnitPluginTest extends AbstractJUnitTest {
    @Test
    public void publish_test_result_which_passed() {
        FreeStyleJob j = jenkins.jobs.create();
        j.configure();
        j.copyResource(resource("/junit/success/com.simple.project.AppTest.txt"));
        j.copyResource(resource("/junit/success/TEST-com.simple.project.AppTest.xml"));
        j.addPublisher(JUnitPublisher.class).testResults.set("*.xml");
        j.save();

        Build b = j.startBuild().shouldSucceed();
        b.open();
        TestReport testReport = b.action(TestReport.class).openViaLink();
        assertEquals("There should be 0 failing tests", testReport.getFailedTestCount(), 0);
    }

    @Test
    public void publish_test_result_which_failed() {
        FreeStyleJob j = jenkins.jobs.create();
        j.configure();
        j.copyResource(resource("/junit/failure/com.simple.project.AppTest.txt"));
        j.copyResource(resource("/junit/failure/TEST-com.simple.project.AppTest.xml"));
        j.addPublisher(JUnitPublisher.class).testResults.set("*.xml");
        j.save();

        Build b = j.startBuild();
        assertThat(b.getResult(), is("UNSTABLE"));

        b.open();
        TestReport testReport = b.action(TestReport.class).openViaLink();
        assertEquals("There should be 1 failing test", testReport.getFailedTestCount(), 1);
    }

    @Test
    @Issue("JENKINS-22833")
    public void publish_parametrized_tests() {
        FreeStyleJob j = jenkins.jobs.create();
        j.configure();
        j.copyResource(resource("/junit/parameterized/junit.xml"));
        j.copyResource(resource("/junit/parameterized/testng.xml"));
        j.addPublisher(JUnitPublisher.class).testResults.set("*.xml");
        j.save();

        Build b = j.startBuild();
        assertThat(b.getResult(), is("UNSTABLE"));

        b.open();
        TestReport testReport = b.action(TestReport.class).openViaLink();

        testReport.assertFailureContent("JUnit.testScore[0]", "expected:<42> but was:<0>");
        testReport.assertFailureContent("JUnit.testScore[1]", "expected:<42> but was:<1>");
        testReport.assertFailureContent("JUnit.testScore[2]", "expected:<42> but was:<2>");

        testReport.assertFailureContent("TestNG.testScore", "expected:<42> but was:<0>");
        testReport.assertFailureContent("TestNG.testScore", "expected:<42> but was:<1>");
        testReport.assertFailureContent("TestNG.testScore", "expected:<42> but was:<2>");
    }
}
