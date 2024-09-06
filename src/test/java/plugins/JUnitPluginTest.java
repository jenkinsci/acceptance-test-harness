package plugins;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.jenkinsci.test.acceptance.Matchers.hasContent;

import java.util.ArrayList;
import java.util.List;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.JUnitPublisher;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;
import org.openqa.selenium.WebElement;

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

        j.startBuild().shouldSucceed().open();

        clickLink("Test Result");
        assertThat(driver, hasContent("0 failures"));
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
        clickLink("Test Result");
        assertThat(driver, hasContent("1 failures"));
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
        clickLink("Test Result");
        assertMessage("JUnit.testScore[0]", "expected:<42> but was:<0>");
        assertMessage("JUnit.testScore[1]", "expected:<42> but was:<1>");
        assertMessage("JUnit.testScore[2]", "expected:<42> but was:<2>");

        assertMessage("TestNG.testScore", "expected:<42> but was:<0>");
        assertMessage("TestNG.testScore", "expected:<42> but was:<1>");
        assertMessage("TestNG.testScore", "expected:<42> but was:<2>");
    }

    private void assertMessage(String test, String content) {
        // Given that there may be several tests with the same name, we assert
        // that at least one of the pages have the requested content
        final List<WebElement> elements = all(by.xpath("//a[text()='%s']", test));
        final List<String> testPages = new ArrayList<String>(elements.size());
        for (WebElement e : elements) {
            testPages.add(e.getAttribute("href"));
        }
        boolean found = false;
        for (String page : testPages) {
            driver.get(page);
            found = hasContent(content).matchesSafely(driver);
            driver.navigate().back();
            if (found) {
                break;
            }
        }
        assertThat("No test found with given content", found);
    }
}
