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

    /**
     Scenario: Run batch task manually
       Given I have installed the "batch-task" plugin
       And a job
       When I configure the job
       And I add batch task "manual"
       And I add batch task "useless"
       And I save the job
       And I build the job
       And I run "manual" batch task manually
       Then the batch task "manual" should run
       Then the batch task "useless" should not run
     */
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

    /**
     Scenario: Trigger batch task
       Given I have installed the "batch-task" plugin
       And a job
       When I configure the job
       And I add batch task "runit"
       And I add batch task "dontrunit"
       And I configure batch trigger for "runit"
       And I save the job
       And I build the job
       Then the build should succeed
       And the batch task "runit" should run
       And the batch task "dontrunit" should not run
     */
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

    /**
     Scenario: Trigger batch task on other job
       Given I have installed the "batch-task" plugin
       When I create a job named "target"
       And I configure the job
       And I add batch task "runit"
       And I add batch task "dontrunit"
       And I save the job
       And I build the job

       And I create a job named "trigger"
       And I configure the job
       And I configure "target" batch trigger for "runit"
       And I save the job
       And I build the job

       Then the build should succeed
       And "target" batch task "runit" should run
       And "target" batch task "dontrunit" should not run
     */
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

    /**
     Scenario: Do not trigger for failed build
       Given I have installed the "batch-task" plugin
       And a job
       And I configure the job
       And I add batch task "dontrunit"
       And I add always fail build step
       And I configure batch trigger for "dontrunit"
       And I save the job
       And I build the job
       Then the batch task "dontrunit" should not run
     */
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


    private void configureBatchTrigger(Job job, BatchTask task) {
        job.save();
        // Needed to save configured batch tasks before configuring triggers
        job.configure();
        job.addPublisher(BatchTaskTrigger.class).setTask(task);
    }

    private BatchTaskDeclaration addBatchTask(String name) {
        return BatchTaskDeclaration.add(job, name);
    }

    private BatchTask task(String name) {
        return new BatchTask(job, name);
    }
}
