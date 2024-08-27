package org.jenkinsci.test.acceptance.po;

import java.net.URL;
import java.time.Duration;
import java.util.concurrent.Callable;
import org.jenkinsci.test.acceptance.selenium.Scroller;
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
    
    public WebElement findTypeCaption(Class<?> type) {
        return findCaption(type, getFinder());
    }

    public <T extends TopLevelItem> T create(Class<T> type, String name) {
        visit("newJob");
        fillIn("name", name);

        findTypeCaption(type).click();

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

        // Automatic disabling of sticky elements doesn't seem to occur after a redirect,
        // so force it after the configuration page has loaded
        new Scroller(driver).disableStickyElements();

        final T j = get(type, name);

        // I'm seeing occasional 404 when trying to access the page right after a job is created.
        // so I'm giving it a bit of time before the job properly appears.
        waitFor().withTimeout(Duration.ofSeconds(3))
                .until((Callable<Object>) () -> {
                    try {
                        j.getJson();
                        return true;
                    } catch (Exception e) {
                        return false;
                    }
                });

        return j;
    }

    public <T extends TopLevelItem> T get(Class<T> type, String name) {
        if (contextAvailable() && typeAcceptsContext(type)) {
            return newInstance(type, getContext(), url("job/%s/", name), name);
        }
        return newInstance(type, injector, url("job/%s/", name), name);
    }

    private <T extends TopLevelItem> boolean typeAcceptsContext(Class<T> type) {
        try {
            type.getConstructor(PageObject.class, URL.class, String.class);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    private boolean contextAvailable() {
        return getContext() != null;
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
        fillIn("from", from);
        // There is a javascript magic bound to loss of focus on 'from' field that is a pain to duplicate through selenium
        // explicitly. Here, it is done so by setting 'to' afterwards.
        fillIn("name", to);
        clickButton("OK");
    }
    
    private final Finder<WebElement> finder = new Finder<>() {
        @Override protected WebElement find(String caption) {
            String normalizedCaption = caption.replace('.', '_');
            return outer.find(by.css("li." + normalizedCaption));
        }
    };
    
    private Finder<WebElement> getFinder() {
        return finder;
    }
}
