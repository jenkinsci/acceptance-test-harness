package org.jenkinsci.test.acceptance.plugins.batch_task;

import org.jenkinsci.test.acceptance.po.BuildStepPageObject;
import org.jenkinsci.test.acceptance.po.Job;
import org.jenkinsci.test.acceptance.po.PostBuildStep;

/**
 * @author Kohsuke Kawaguchi
 */
@BuildStepPageObject("Invoke batch tasks")
public class BatchTaskTrigger extends PostBuildStep {
    public BatchTaskTrigger(Job parent, String path) {
        super(parent, path);
    }

    public void setTask(BatchTask task) {
        control("configs/project").sendKeys(task.job.name);
        control("configs/task").click();
        control("configs/task").sendKeys(task.name);
    }

    public void allowUnstable() {
        check(control("evenIfUnstable"));
    }
}
