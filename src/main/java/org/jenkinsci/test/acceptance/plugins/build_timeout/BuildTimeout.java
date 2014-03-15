package org.jenkinsci.test.acceptance.plugins.build_timeout;

import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Job;
import org.jenkinsci.test.acceptance.po.PageArea;

/**
 * Build timeout plugin setting in the job config page.
 */
public class BuildTimeout extends PageArea {
    private final Job job;

    public final Control enable = control("");

    public final Control writingDescription = control("writingDescription");

    public final Control failBuild = control("failBuild");

    public BuildTimeout(Job job) {
        super(job, "/hudson-plugins-build_timeout-BuildTimeoutWrapper");
        this.job = job;
    }

    public void abortAfter(int timeout) {
        ensureActive();
        choose("Absolute");
        fillIn("_.timeoutMinutes",timeout);
    }

    public void abortWhenStuck() {
        ensureActive();
        choose("Likely stuck");
    }

    public void ensureActive() {
        job.ensureConfigPage();
        enable.click();
    }
}
