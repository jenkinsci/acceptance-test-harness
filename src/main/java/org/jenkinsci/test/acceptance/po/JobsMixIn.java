package org.jenkinsci.test.acceptance.po;

import com.google.inject.Injector;
import org.openqa.selenium.By;

import java.net.URL;

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

    public <T extends Job> T create(Class<T> type, String name) {
        String sut_type = type.getAnnotation(JobPageObject.class).value();

        visit("newJob");
        fillIn("name", name);
        find(By.xpath("//input[starts-with(@value, '" + sut_type + "')]")).click();
        clickButton("OK");

        return get(type, name);
    }

    public <T extends Job> T get(Class<T> type, String name) {
        try {
            return type.getConstructor(Injector.class,URL.class,String.class)
                    .newInstance(injector, url("job/%s/", name), name);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    public FreeStyleJob create() {
        return create(FreeStyleJob.class);
    }

    public <T extends Job> T create(Class<T> type) {
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
