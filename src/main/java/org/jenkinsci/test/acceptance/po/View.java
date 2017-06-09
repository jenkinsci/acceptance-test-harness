package org.jenkinsci.test.acceptance.po;

import java.net.URL;

import org.hamcrest.Description;
import org.jenkinsci.test.acceptance.Matcher;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Injector;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.jenkinsci.test.acceptance.Matchers.*;

/**
 * Page object for view, which is a collection of jobs rendered in the UI.
 * <p/>
 * Use {@link Describable} annotation to register an implementation.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class View extends ContainerPageObject {
    private final Control recurseIntoFolder = control("/recurse");

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
        control("/useincluderegex").check();
        String matchEverything = ".*";
        Control regexJobFilter = control("/useincluderegex/includeRegex");
        regexJobFilter.set(matchEverything);
    }

    /**
     * Deletes the view.
     */
    public void delete() {
        configure();
        clickLink("Delete View");
        waitFor(by.button("Yes"));
        clickButton("Yes");
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
}
