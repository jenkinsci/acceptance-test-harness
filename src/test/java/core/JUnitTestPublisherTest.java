package core;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.Bug;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.JUnitPublisher;
import org.junit.Test;
import org.openqa.selenium.WebElement;

import java.util.Iterator;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
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

        j.startBuild().shouldSucceed().open();

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

        Build b = j.startBuild();
        assertThat(b.getResult(), is("UNSTABLE"));

        b.open();
        clickLink("Test Result");
        assertThat(driver, hasContent("1 failures"));
    }

    /**
     Scenario: Publish rest of parameterized tests
       Given a job
       When I configure the job
       And I copy resource "junit/parameterized" into workspace
       And I set Junit archiver path "parameterized/*.xml"
       And I save the job
       And I build the job
       Then the build should be unstable
       And I visit build action named "Test Result"
       And "JUnit.testScore[0]" error summary should match "expected:<42> but was:<0>"
       And "JUnit.testScore[1]" error summary should match "expected:<42> but was:<1>"
       And "JUnit.testScore[2]" error summary should match "expected:<42> but was:<2>"
       And "TestNG.testScore" error summary should match "expected:<42> but was:<0>"
       And "TestNG.testScore" error summary should match "expected:<42> but was:<1>"
       And "TestNG.testScore" error summary should match "expected:<42> but was:<2>"
     */
    @Test @Bug("22833")
    public void publish_rest_of_parameterized_tests() {
        FreeStyleJob j = jenkins.jobs.create();
        j.configure();
        j.copyResource(resource("/junit/parameterized/junit.xml"));
        j.copyResource(resource("/junit/parameterized/testng.xml"));
        j.addPublisher(JUnitPublisher.class).testResults.set("*.xml");
        j.save();

        Build b = j.startBuild();
        assertThat(b.getResult(), is("UNSTABLE"));

        b.open();
        clickLink("Test Result");
        assertMessage("JUnit.testScore[0]","expected:<42> but was:<0>");
        assertMessage("JUnit.testScore[1]","expected:<42> but was:<1>");
        assertMessage("JUnit.testScore[2]","expected:<42> but was:<2>");

        assertMessage("TestNG.testScore","expected:<42> but was:<0>");
        assertMessage("TestNG.testScore","expected:<42> but was:<1>");
        assertMessage("TestNG.testScore","expected:<42> but was:<2>");
    }

    private void assertMessage(String test, String msg) {
        toggle(test);
        assertThat(driver, hasContent(msg));
        toggle(test);
    }

    private void toggle(String test) {
        List<WebElement> elements = all(by.xpath("//a[text()='%s']/../a[starts-with(@href, 'javascript')]", test));

        for (Iterator<WebElement> itr = elements.iterator(); itr.hasNext(); ) {
            WebElement e =  itr.next();
            if (!e.isDisplayed())
                itr.remove();
        }

        assert(!elements.isEmpty());
        for (WebElement e : elements) {
            e.click();
        }
    }
}
