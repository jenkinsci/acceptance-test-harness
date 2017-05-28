package org.jenkinsci.test.acceptance.po;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.WebElement;

/**
 * Mix-in for {@link PageObject}s that own a group of views, like
 * {@link Jenkins}.
 *
 * @author Kohsuke Kawaguchi
 */
public class ViewsMixIn extends MixIn {
    public ViewsMixIn(ContainerPageObject context) {
        super(context);
    }

    public <T extends View> T create(final Class<T> type) {
        return create(type, createRandomName());
    }

    public <T extends View> T create(final Class<T> type, String name) {

        final Finder<WebElement> finder = new Finder<WebElement>() {
            @Override protected WebElement find(String caption) {
                return outer.find(by.radioButton(caption));
            }
        };

        // Views contributed by plugins might need some extra time to appear
        WebElement typeRadio = waitFor().withTimeout(5, TimeUnit.SECONDS)
                .until(new Callable<WebElement>() {
                    @Override public WebElement call() throws Exception {
                        visit("newView");
                        return findCaption(type, finder);
                    }
        });

        typeRadio.click();

        fillIn("name", name);

        clickButton("OK");

        return newInstance(type, injector, url("view/%s/", name));
    }
}
