package plugins;


import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.tasks.TaskScannerAction;
import org.jenkinsci.test.acceptance.plugins.tasks.TaskScannerPublisher;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;

/**
 Feature: Scan for open tasks
 In order to be able to collect and analyse open tasks.
 As a Jenkins user
 I want to install and configure Task Scanner plugin

  @author Martin Ende
 */
@WithPlugins("tasks")
public class TaskScannerPluginTest extends AbstractCodeStylePluginHelper{

    /**
     * This test's objective is to verify the basic functionality of the Task
     * Scanner plugin, i.e. finding different task tags, including / excluding
     * files and providing the correct results.
     * The test builds the same job twice with and without case sensitivity.
     */

    @Test
    public void single_task_tags_and_exclusion_pattern() throws Exception{
        //TODO: create an appropriate setupJob method
        FreeStyleJob j = jenkins.jobs.create();

        j.configure();

        //FIXME: insertion of the shell step text takes too long. firefox considers the script to be unresponsive.
        //       It seems that using copyResource() with a manually generated tar archive is a bit
        //       faster than copyDir(), but it is not really stable. A workaround is to place a breakpoint
        //       at the addShellStep statement inside copyResource, doing this as single step and then
        //       proceeding with the test.
        //       Nevertheless, we should find a better way to get the files into the workspace

        //j.copyDir(resource("/tasks_plugin/fileset1"));
        j.copyResource(resource("/tasks_plugin/fileset1.tar"));
        j.addShellStep("tar -xvf fileset1.tar"); //necessary when using copyResource with tar file
        TaskScannerPublisher pub = j.addPublisher(TaskScannerPublisher.class);
        pub.pattern.set("**/*.java");
        pub.excludePattern.set("**/*Test.java");
        pub.highPriorityTags.set("FIXME");
        pub.normalPriorityTags.set("TODO");
        pub.lowPriorityTags.set("@Deprecated");
        pub.ignoreCase.uncheck();
        j.save();

        // as no threshold is defined to mark the build as FAILED or UNSTABLE, the build should succeed
        Build lastBuild = buildJobWithSuccess(j);
        lastBuild.open();
        TaskScannerAction tsa = new TaskScannerAction(j);

        // The file set consists of 9 files, whereof
        //   - 2 file names match the exclusion pattern
        //   - 7 files are to be scanned for tasks
        //   - 5 files actually contain tasks with the specified tags
        //
        // The expected task priorities are:
        //   - 1x high
        //   - 4x medium
        //   - 1x low

        assertThat(tsa.getResultLinkByXPathText("6 open tasks"), is("tasksResult"));
        assertThat(tsa.getResultTextByXPathText("6 open tasks"), endsWith("in 7 workspace files."));
        assertThat(tsa.getWarningNumber(), is(6));
        assertThat(tsa.getHighWarningNumber(), is(1));
        assertThat(tsa.getNormalWarningNumber(), is(4));
        assertThat(tsa.getLowWarningNumber(), is(1));

        tsa.assertFilesTab("fileset1_eval1");
        tsa.assertTypesTab("fileset1_eval1");
        tsa.assertWarningsTab("fileset1_eval1");

        // now disable case sensitivity and build again. Then the publisher shall also
        // find the high priority task in Ec2Provider.java:133.

        j.configure();
        pub.ignoreCase.check();
        j.save();

        lastBuild = buildJobWithSuccess(j);

        lastBuild.open();
        assertThat(tsa.getResultLinkByXPathText("7 open tasks"), is("tasksResult"));
        assertThat(tsa.getResultTextByXPathText("7 open tasks"), endsWith("in 7 workspace files."));
        assertThat(tsa.getResultLinkByXPathText("1 new open task"), is("tasksResult/new"));
        assertThat(tsa.getWarningNumber(), is(7));
        assertThat(tsa.getNewWarningNumber(), is(1));
        assertThat(tsa.getHighWarningNumber(), is(2));
        assertThat(tsa.getNormalWarningNumber(), is(4));
        assertThat(tsa.getLowWarningNumber(), is(1));

        lastBuild.visit(tsa.getNewWarningsUrlAsRelativePath());
        assertThat(tsa.getResultLinkByXPathText("Ec2Provider.java:133"), startsWith("source"));
    }


    //TODO: Test for JENKINS-22744: https://issues.jenkins-ci.org/browse/JENKINS-22744

}
