package core;

import org.hamcrest.CoreMatchers;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.JUnitPublisher;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.jenkinsci.test.acceptance.Matchers.*;

/**
 * @author Kohsuke Kawaguchi
 */
public class JUnitTestPublisherTest extends AbstractJUnitTest {
    /**
     Scenario: Publish test result which passed
       When I create a job named "javadoc-test"
       And I configure the job
       And I copy resource "junit/success" into workspace
       And I set Junit archiver path "success/*.xml"
       And I save the job
       And I build the job
       Then the build should succeed
       And I visit build action named "Test Result"
       Then the page should say "0 failures"
     */
    @Test
    public void publish_test_result_which_passed() {
        FreeStyleJob j = jenkins.jobs.create();
        j.configure();
        j.copyResource(resource("/junit/success/com.simple.project.AppTest.txt"));
        j.copyResource(resource("/junit/success/TEST-com.simple.project.AppTest.xml"));
        j.addPublisher(JUnitPublisher.class).testResults.set("*.xml");
        j.save();

        j.queueBuild().shouldSucceed().open();

        clickLink("Test Result");
        assertThat(driver, hasContent("0 failures"));
    }

    /**
     Scenario: Publish test result which failed
       When I create a job named "javadoc-test"
       And I configure the job
       And I copy resource "junit/failure" into workspace
       And I set Junit archiver path "failure/*.xml"
       And I save the job
       And I build the job
       Then the build should be unstable
       And I visit build action named "Test Result"
       Then the page should say "1 failures"
     */
    @Test
    public void publish_test_result_which_failed() {
        FreeStyleJob j = jenkins.jobs.create();
        j.configure();
        j.copyResource(resource("/junit/failure/com.simple.project.AppTest.txt"));
        j.copyResource(resource("/junit/failure/TEST-com.simple.project.AppTest.xml"));
        j.addPublisher(JUnitPublisher.class).testResults.set("*.xml");
        j.save();

        Build b = j.queueBuild();
        assertThat(b.getResult(), is("UNSTABLE"));

        b.open();
        clickLink("Test Result");
        assertThat(driver, hasContent("1 failures"));
    }
}
