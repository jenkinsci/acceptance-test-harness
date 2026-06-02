package org.jenkinsci.test.acceptance.plugins.junit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;

import java.util.ArrayList;
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
        assertThat("No test found with name " + test, rows, not(empty()));

        List<String> contents = new ArrayList<>();
        for (WebElement row : rows) {
            // this is a javascript expand
            WebElement link = row.findElement(By.tagName("a"));
            link.click();
            WebElement details =
                    waitFor(row).ignoring(NoSuchElementException.class).until(TestReport::getTestDetailsRow);
            String c = details.getText();
            link.click(); // hide the content
            waitFor(details).until(CapybaraPortingLayerImpl::isStale);
            if (c.contains(content)) {
                // we found what we were looking for; return
                return;
            }
            // not our needle, save for later diagnostics
            contents.add(c);
        }
        // if we got here we know that we have failed.
        // but still assert so that we have a better idea of what we did find
        assertThat("No test found with given content", contents, hasItem(containsString(content)));
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
