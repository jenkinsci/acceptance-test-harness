package org.jenkinsci.test.acceptance.po;

import java.time.Duration;
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
            @Override
            protected WebElement find(String caption) {
                return outer.find(by.radioButton(caption));
            }
        };

        // Views contributed by plugins might need some extra time to appear
        WebElement typeRadio = waitFor().withTimeout(Duration.ofSeconds(5)).until(() -> {
            visit("newView");
            return findCaption(type, finder);
        });

        typeRadio.click();

        fillIn("name", name);

        clickButton("Create");

        return newInstance(type, injector, url("view/%s/", name));
    }

    /**
     * Returns the page object of a view.
     *
     * @param type The class object of the type of the view..
     * @param name The name of the view.
     * @param <T> The type of the view.
     *
     * @return page object of a view to the corresponding type and name.
     */
    public <T extends View> T get(Class<T> type, String name) {
        return newInstance(type, injector, url("view/%s/", name));
    }
}
