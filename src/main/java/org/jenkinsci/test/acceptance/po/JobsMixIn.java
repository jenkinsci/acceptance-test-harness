package org.jenkinsci.test.acceptance.po;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

/**
 * Mix-in for {@link PageObject}s that own a group of jobs, like
 * {@link Jenkins}, {@link View}, etc.
 *
 * @author Kohsuke Kawaguchi
 */
public class JobsMixIn extends MixIn {
    public JobsMixIn(ContainerPageObject context) {
        super(context);
    }

    public <T extends TopLevelItem> T create(Class<T> type, String name) {
        visit("newJob");
        fillIn("name", name);

        findCaption(type, new Finder<WebElement>() {
            @Override protected WebElement find(String caption) {
                try {
                    // Jenkins 2.0 introduced a new "new item" page, which listed
                    // the item types differently and did away with the radio buttons.
                    String normalizedCaption = caption.replace('.', '_');
                    return outer.find(by.css("li." + normalizedCaption));
                } catch (NoSuchElementException e) {
                    // Jenkins 1.x item type selection was by radio button.
                    return outer.find(by.radioButton(caption));
                }
            }
        }).click();

        clickButton("OK");

        final T j = get(type, name);

        // I'm seeing occasional 404 when trying to access the page right after a job is created.
        // so I'm giving it a bit of time before the job properly appears.
        waitFor().withTimeout(3, TimeUnit.SECONDS)
                .until(new Callable<Object>() {
                    @Override
                    public Object call() throws Exception {
                        try {
                            j.getJson();
                            return true;
                        } catch (Exception e) {
                            return false;
                        }
                    }
        });

        return j;
    }

    public <T extends TopLevelItem> T get(Class<T> type, String name) {
        return newInstance(type, injector, url("job/%s/", name), name);
    }

    public FreeStyleJob create() {
        return create(FreeStyleJob.class);
    }

    public <T extends TopLevelItem> T create(Class<T> type) {
        return create(type, createRandomName());
    }

    public void copy(Job from, String to) {
        copy(from.name, to);
    }

    public void copy(String from, String to) {
        visit("newJob");
        fillIn("name",to);
        check(find(by.radioButton("copy")));
        fillIn("from",from);
        clickButton("OK");
    }
}
