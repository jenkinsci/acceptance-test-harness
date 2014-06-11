package org.jenkinsci.test.acceptance.plugins.batch_task;

import org.jenkinsci.test.acceptance.po.*;

/**
 * @author Kohsuke Kawaguchi
 */
@Describable("Invoke batch tasks")
public class BatchTaskTrigger extends AbstractStep implements PostBuildStep {
    public final Control allowUnstable = control("evenIfUnstable");
    public final Control project = control("configs/project");
    public final Control task = control("configs/task");

    public BatchTaskTrigger(Job parent, String path) {
        super(parent, path);
    }

    public void setTask(BatchTask t) {
        project.set(t.job.name);
        task.click();
        task.select(t.name);
    }
}
