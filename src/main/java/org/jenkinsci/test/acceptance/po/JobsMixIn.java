package org.jenkinsci.test.acceptance.po;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import org.openqa.selenium.By;
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

        findCaption(type, getFinder()).click();

        // since 2.7, a bug in Firefox webdriver may prevent the blur event from triggering
        // properly, so we manually execute a blur event here, see: JENKINS-37232
        // See: https://github.com/seleniumhq/selenium-google-code-issue-archive/issues/7346
        try {
            blur(find(By.name("name")));
        } catch (Exception e) {
            // This should rarely fail on modern browsers,
            // we don't really care if it does since Firefox was 
            // the only one that seemed to exhibit the issue
        }

        clickButton("OK");
        // Sometimes job creation is not fast enough, so make sure it's finished before continue
        waitFor(by.name("config"), 10);

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
        fillIn("from",from);
        if (getJenkins().isJenkins1X()) { 
            // no radio buttons on Jenkins 2.X
            choose("copy");
        } else {
            // a bit hacky: send a tab to enable the OK button
            find(By.name("from")).sendKeys("\t");
        }
        clickButton("OK");
    }
    
    // Jenkins 1.x item type selection was by radio button.
    final Finder<WebElement> finder1X = new Finder<WebElement>() {
        @Override protected WebElement find(String caption) {
            return outer.find(by.radioButton(caption));
        }
    };
    
    // Jenkins 2.0 introduced a new "new item" page, which listed
    // the item types differently and did away with the radio buttons.
    final Finder<WebElement> finder2X = new Finder<WebElement>() {
        @Override protected WebElement find(String caption) {
            String normalizedCaption = caption.replace('.', '_');
            return outer.find(by.css("li." + normalizedCaption));
        }
    };
    
    private Finder<WebElement> getFinder() {
        return getJenkins().isJenkins1X() ? finder1X : finder2X;
    }
}
