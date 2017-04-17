package org.jenkinsci.test.acceptance.po;

import org.apache.commons.lang3.StringUtils;

/**
 * Triggers a job, if another job has been finished.
 *
 * @author Ulli Hafner
 */
public class UpstreamJobTrigger extends Trigger {
    private final Control upstreamProjects = control("upstreamProjects");

    public UpstreamJobTrigger(Job parent) {
        super(parent, "/jenkins-triggers-ReverseBuildTrigger");
    }

    public void setUpstreamProjects(final String... upstreamProjects) {
        this.upstreamProjects.set(StringUtils.join(upstreamProjects, ","));
    }
}
