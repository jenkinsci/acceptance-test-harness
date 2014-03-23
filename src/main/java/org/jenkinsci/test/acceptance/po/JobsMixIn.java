package org.jenkinsci.test.acceptance.po;

import java.util.concurrent.Callable;

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
        find(by.radioButton(sut_type)).click();
        clickButton("OK");

        final T j = get(type, name);

        // I'm seeing occasional 404 when trying to access the page right after a job is created.
        // so I'm giving it a big of time before the job properly appears.
        j.waitForCond(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                try {
                    j.getJson();
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }
        },3);

        return j;
    }

    public <T extends Job> T get(Class<T> type, String name) {
        return newInstance(type, injector, url("job/%s/", name), name);
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
