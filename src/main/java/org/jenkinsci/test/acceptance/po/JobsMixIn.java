package org.jenkinsci.test.acceptance.po;

import java.net.URL;
import java.time.Duration;
import java.util.concurrent.Callable;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

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

        // the OK button is enabled in reaction to the click on the type
        // so wait to ensure the click goes to an enabled button
        WebElement okButton = waitFor(driver).until(ExpectedConditions.elementToBeClickable(by.button("OK")));
        okButton.click();

        // Sometimes job creation is not fast enough, so make sure it's finished before continue
        waitFor(by.name("config"), 10);

        final T j = get(type, name);

        // I'm seeing occasional 404 when trying to access the page right after a job is created.
        // so I'm giving it a bit of time before the job properly appears.
        waitFor().withTimeout(Duration.ofSeconds(3)).until((Callable<Object>) () -> {
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

        fillIn("name", to);

        find(by.radioButton("Duplicate an existing item")).click();
        fillIn("from", from);

        // do not wait for the OK button to be enabled
        // some tests check that this fails due to the job already existing.
        clickButton("OK");
    }

    private final Finder<WebElement> finder = new Finder<>() {
        @Override
        protected WebElement find(String caption) {
            String normalizedCaption = caption.replace('.', '_');
            return outer.find(by.css("." + normalizedCaption));
        }
    };

    private Finder<WebElement> getFinder() {
        return finder;
    }
}
