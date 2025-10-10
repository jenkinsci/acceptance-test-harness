package org.jenkinsci.test.acceptance.plugins.junit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;

import java.util.List;
import org.jenkinsci.test.acceptance.po.Action;
import org.jenkinsci.test.acceptance.po.ActionPageObject;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.CapybaraPortingLayerImpl;
import org.jenkinsci.test.acceptance.po.ContainerPageObject;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

@ActionPageObject(relativePath = "testReport", linkText = "Tests")
public class TestReport extends Action {

    public TestReport(Build build, String path, String linkText) {
        super(build, path, linkText);
    }

    public int getTotalTestCount() {
        // ugly hack as the junit plugin is not super friendly for testing.
        WebElement tests = find(by.xpath("//span[class='jenkins-app-bar__subtitle']"));
        return Integer.parseInt(tests.getText());
    }

    public int getSkippedTestCount() {
        WebElement tests = getElement(by.xpath("//div[contains(@title, ' skipped')]"));
        if (tests == null) {
            return 0;
        }
        return Integer.parseInt(tests.getText());
    }

    public int getFailedTestCount() {
        WebElement tests = getElement(by.xpath("//div[contains(@title, ' failing')]"));
        if (tests == null) {
            return 0;
        }
        return Integer.parseInt(tests.getText());
    }

    public void assertFailureContent(String test, String content) {
        // Given that there may be several tests with the same name, we assert
        // that at least one of the pages have the requested content

        final List<WebElement> rows = all(by.xpath("//tr[@data='%s']", test));

        boolean found = false;
        for (WebElement row : rows) {
            // this is a javascript expand
            WebElement link = row.findElement(By.tagName("a"));
            link.click();
            WebElement details =
                    waitFor(row).ignoring(NoSuchElementException.class).until(TestReport::getTestDetailsRow);
            found = details.getText().contains(content);
            link.click(); // hide the content
            waitFor(details).until(CapybaraPortingLayerImpl::isStale);
            if (found) {
                break;
            }
        }
        assertThat("No test found with name " + test, rows, not(empty()));
        assertThat("No test found with given content", found);
    }

    @Override
    public boolean isApplicable(ContainerPageObject po) {
        return po instanceof Build;
    }

    private static WebElement getTestDetailsRow(WebElement testRow) {
        WebElement sibling = testRow.findElement(By.xpath("following-sibling::*[1]"));
        if ("foldout-row".equals(sibling.getDomAttribute("data-type"))) {
            return sibling;
        }
        return null;
    }
}
