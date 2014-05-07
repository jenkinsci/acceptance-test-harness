package org.jenkinsci.test.acceptance.po;

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

    public <T extends View> T create(Class<T> type, String name) {
        visit("newView");
        fillIn("name",name);

        findCaption(type, new Finder<WebElement>() {
            @Override protected WebElement find(String caption) {
                return outer.find(by.radioButton(caption));
            }
        }).click();

        clickButton("OK");

        return newInstance(type, injector, url("view/%s/", name));
    }
}
