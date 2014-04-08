package org.jenkinsci.test.acceptance.po;

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
        String sut_type = type.getAnnotation(Describable.class).value();

        visit("newView");
        fillIn("name",name);
        check(find(by.radioButton(sut_type)));
        clickButton("OK");

        return newInstance(type, injector, url("view/%s/", name));
    }
}
