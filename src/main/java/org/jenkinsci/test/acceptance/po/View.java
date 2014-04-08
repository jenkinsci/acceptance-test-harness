package org.jenkinsci.test.acceptance.po;

import com.google.inject.Injector;

import java.net.URL;

import static org.hamcrest.CoreMatchers.not;
import static org.jenkinsci.test.acceptance.Matchers.hasContent;

/**
 * Page object for view, which is a collection of jobs rendered in the UI.
 *
 * @author Kohsuke Kawaguchi
 */
public class View extends ContainerPageObject {
    public final JobsMixIn jobs;

    public View(Injector injector, URL url) {
        super(injector, url);
        jobs = new JobsMixIn(this);
    }

    /**
     * Clicks a build button for a job of the specified name.
     */
    public void build(String name) {
        find(by.xpath("//a[contains(@href, '/%s/build?')]/img[contains(@title, 'Schedule a build')]",name)).click();
    }


    @Override
    public void save() {
        clickButton("OK");
        assertThat(driver, not(hasContent("This page expects a form submission")));
    }
}
