package plugins;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.batch_task.BatchTask;
import org.jenkinsci.test.acceptance.plugins.batch_task.BatchTaskDeclaration;
import org.jenkinsci.test.acceptance.plugins.batch_task.BatchTaskTrigger;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.Job;
import org.jenkinsci.test.acceptance.po.ShellBuildStep;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.NoSuchElementException;

import com.google.common.base.Function;

/**
 * Batch-task plugin test.
 */
@WithPlugins("batch-task")
public class BatchTaskPluginTest extends AbstractJUnitTest {
    FreeStyleJob job;

    @Before
    public void setUp() {
        job = jenkins.jobs.create();
    }

    @Test
    public void run_batch_task_manually() {
        job.configure();
        addBatchTask("manual");
        addBatchTask("useless");
        job.save();
        job.startBuild().waitUntilFinished();
        task("manual").build();

        task("manual").shouldExist();
        task("useless").shouldNotExist();
    }

    @Test
    public void trigger_batch_task() {
        job.configure();
        addBatchTask("runit");
        addBatchTask("dontrunit");
        configureBatchTrigger(job,task("runit"));
        job.save();

        job.startBuild().waitUntilFinished().shouldSucceed();
        task("runit").shouldExist();
        task("dontrunit").shouldNotExist();
    }

    @Test
    public void trigger_batch_task_on_other_job() {
        job.configure();
        addBatchTask("runit");
        addBatchTask("dontrunit");
        job.save();
        job.startBuild().shouldSucceed();

        FreeStyleJob trigger = jenkins.jobs.create();
        trigger.configure();
        configureBatchTrigger(trigger,task("runit"));
        trigger.save();
        trigger.startBuild().shouldSucceed();

        task("runit").shouldExist();
        task("dontrunit").shouldNotExist();
    }

    @Test
    public void do_not_trigger_for_failed_build() {
        job.configure();
        addBatchTask("dontrunit");
        job.addBuildStep(ShellBuildStep.class).command("false");
        configureBatchTrigger(job,task("dontrunit"));
        job.save();
        job.startBuild().waitUntilFinished();
        task("dontrunit").shouldNotExist();
    }


    private void configureBatchTrigger(Job job, final BatchTask task) {
        job.save(); // Needed to save configured batch tasks before configuring triggers

        // Configured tasks are sometimes missing immediately after save
        job.waitFor(job)
                .ignoring(NoSuchElementException.class)
                .until(new Function<Job, Boolean>() {
                    @Override public Boolean apply(Job job) {
                        job.configure();
                        job.addPublisher(BatchTaskTrigger.class).setTask(task);
                        return true;
                    }
        });
    }

    private BatchTaskDeclaration addBatchTask(String name) {
        return BatchTaskDeclaration.add(job, name);
    }

    private BatchTask task(String name) {
        return new BatchTask(job, name);
    }
}
