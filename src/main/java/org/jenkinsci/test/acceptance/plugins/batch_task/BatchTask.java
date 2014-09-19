package org.jenkinsci.test.acceptance.plugins.batch_task;

import org.jenkinsci.test.acceptance.po.ContainerPageObject;
import org.jenkinsci.test.acceptance.po.Job;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * @author Kohsuke Kawaguchi
 */
public class BatchTask extends ContainerPageObject {
    public final Job job;
    public final String name;

    public BatchTask(Job job, String name) {
        super(job, job.url("batchTasks/task/%s/", name));
        this.job = job;
        this.name = name;
    }

    public void build() {
        open();
        clickLink("Build Now");
        elasticSleep(1000);    // TODO: properly wait until the task is completed
    }

    public boolean exists() {
        open();
        return driver.getPageSource().contains("#1-1");
    }

    public void shouldExist() {
        assertThat(exists(), is(true));
    }

    public void shouldNotExist() {
        assertThat(exists(), is(false));
    }
}
