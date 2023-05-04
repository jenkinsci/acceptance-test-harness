package org.jenkinsci.test.acceptance.po;

import java.net.URL;

import org.hamcrest.Description;
import org.jenkinsci.test.acceptance.Matcher;
import org.openqa.selenium.By;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebElement;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Injector;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.jenkinsci.test.acceptance.Matchers.*;

/**
 * Page object for view, which is a collection of jobs rendered in the UI.
 * <p>
 * Use {@link Describable} annotation to register an implementation.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class View extends ContainerPageObject {
    private final Control recurseIntoFolder = control("/recurse");
    public final Control includeRegex = control("/useincluderegex/includeRegex");

    public final JobsMixIn jobs;

    public View(Injector injector, URL url) {
        super(injector, url);
        jobs = new JobsMixIn(this);
    }

    /**
     * Clicks a build button for a job of the specified name.
     */
    public void build(String name) {
        find(by.xpath("//a[contains(@href, '/%s/build?')]/img[contains(@title, 'Schedule a')]", name)).click();
    }

    /**
     * Configures the view to include all jobs.
     */
    public void matchAllJobs() {
        matchJobs(".*");
    }

    public void matchJobs(String regex) {
        control("/useincluderegex").check();
        includeRegex.set(regex);
    }

    /**
     * Deletes the view.
     */
    public void delete() {
        configure();
        try {
            visit("delete");
            waitFor(by.button("Yes"));
            clickButton("Yes");
        } catch (UnhandledAlertException uae) {
            runThenConfirmAlert(() -> clickLink("Delete View"), 2);
        }
    }

    @Override
    public void save() {
        clickButton("OK");
        assertThat(driver, not(hasContent("This page expects a form submission")));
    }

    public BuildHistory getBuildHistory() {
        return new BuildHistory(this);
    }

    public static Matcher<View> containsJob(final Job needle) {
        return new Matcher<View>("Contains job " + needle.name) {
            @Override
            public boolean matchesSafely(View view) {
                for (JsonNode job: view.getJson().get("jobs")) {
                    String name = job.get("name").asText();
                    if (needle.name.equals(name)) return true;
                }
                return false;
            }

            @Override
            public void describeMismatchSafely(View view, Description mismatchDescription) {
                mismatchDescription.appendText("view containing:");
                for (JsonNode job: view.getJson().get("jobs")) {
                    String name = job.get("name").asText();
                    mismatchDescription.appendText(" ").appendText(name);
                }
            }
        };
    }

    public void checkRecurseIntoFolders() {
        recurseIntoFolder.check();
    }

    /**
     * Sets the description of the current {@link View}.
     *
     * @param description The description of the view.
     */
    public void setDescription(final String description) {
        WebElement descrElem = find(by.name("description"));
        descrElem.clear();
        descrElem.sendKeys(description);
    }

    public String getFormName(){
        return "viewConfig";
    }

    public String getSubmitButtonText(){
        return "OK";
    }

    public static Matcher<View> containsColumnHeaderTooltip(String tooltip) {
        return new Matcher<View>("Contains ToolTip " + tooltip) {
            @Override
            public boolean matchesSafely(View item) {
                WebElement webElement = item.getElement(By.xpath("//th[contains(@tooltip, '" + tooltip + "')]"));
                return webElement != null;
            }
        };
    }

    public static Matcher<View> containsColumnHeader(String headerName) {
        return new Matcher<View>("Contains ToolTip " + headerName) {
            @Override
            public boolean matchesSafely(View item) {
                WebElement webElement = item.getElement(By.xpath("//th/a[text() = '" + headerName + "']"));
                return webElement != null;
            }
        };
    }

    public static Matcher<View> containsImage(String imageName) {
        return new Matcher<View>("Contains image " + imageName) {
            @Override
            public boolean matchesSafely(View item) {
                WebElement webElement = item.getElement(By.xpath("//img[contains(@src, '" + imageName + "')]"));
                return webElement != null;
            }
        };
    }

    public static Matcher<View> containsSvgWithText(String text) {
        return new Matcher<View>("Contains svg with text " + text) {
            @Override
            public boolean matchesSafely(View item) {
                WebElement webElement = item.getElement(
                        By.xpath(String.format("//span[@class = 'jenkins-visually-hidden'][text() = '%s']", text))
                );
                return webElement != null;
            }
        };
    }

    public static Matcher<View> containsLinkWithTooltip(String text) {
        return new Matcher<View>("Contains link with tooltip " + text) {
            @Override
            public boolean matchesSafely(View item) {
                WebElement webElement = item.getElement(
                        By.cssSelector(String.format("a[tooltip='%s']", text))
                );
                return webElement != null;
            }
        };
    }
}
