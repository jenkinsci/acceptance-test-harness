package org.jenkinsci.test.acceptance.plugins.build_timeout;

import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Job;
import org.jenkinsci.test.acceptance.po.PageAreaImpl;
import org.openqa.selenium.NoSuchElementException;

/**
 * Build timeout plugin setting in the job config page.
 */
public class BuildTimeout extends PageAreaImpl {
    private final Job job;

    public final Control failBuild = control("failBuild");

    private final Control addAction = control("hetero-list-add[operationList]");

    public BuildTimeout(Job job) {
        super(job, "/hudson-plugins-build_timeout-BuildTimeoutWrapper");
        this.job = job;
    }

    public void abortAfter(int timeout) {
        ensureActive();
        chooseStrategy("Absolute");
        fillIn("_.timeoutMinutes", timeout);
        abortBuild();
    }

    public void abortWhenStuck() {
        ensureActive();
        chooseStrategy("Likely stuck");
        abortBuild();
    }

    private void ensureActive() {
        job.ensureConfigPage();
        control("").check();
    }

    private void chooseStrategy(String name) {
        control("/").select(name);
    }

    public void abortBuild() {
        if (addAction != null) {
            addAction.click();
            waitFor(by.button("Abort the build"));
            clickButton("Abort the build");
        }
    }

    public void writeDescription() {
        try {
            control("writingDescription").check();
        } catch (NoSuchElementException ex) {
            addAction.click();
            waitFor(by.button("Writing the build description"));
            clickButton("Writing the build description");
        }
    }
}
