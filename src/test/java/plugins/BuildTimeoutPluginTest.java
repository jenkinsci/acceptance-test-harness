package plugins;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.build_timeout.BuildTimeout;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.junit.Test;

/**
 Feature: Fail builds that take too long
   In order to prevent executors from being blocked for too long
   As a Jenkins user
   I want to set timeouts with the build-timeout plugin to abort or
   fail builds that exceed specified timeout values
 */
@WithPlugins("build-timeout")
public class BuildTimeoutPluginTest extends AbstractJUnitTest {
    /**
       # TODO: The build-timeout plugin doesn't allow timeouts less than 3 minutes
       # in duration.
       #JENKINS-19592
       @wip
       Scenario: Fail a blocked build with absolute timeouts
         Given I have installed the "build-timeout" plugin
         And a job
         When I configure the job
         And I add a shell build step "sleep 200"
         And I set the build timeout to 3 minutes
         And I set abort build description
         And I save the job
         And I build the job
         Then the build should fail
     */
    @Test
    public void fail_build_with_absolute_time() {
        FreeStyleJob j = jenkins.jobs.create();
        j.configure();
        {
            j.addShellStep("sleep 300");

            BuildTimeout t = new BuildTimeout(j);
            t.abortAfter(3);
            t.writingDescription.check();
        }
        j.save();

        j.queueBuild().waitUntilFinished(300).shouldAbort();
    }

    /**
     *   #JENKINS-19592
       @wip
       Scenario: Fail a blocked build if likely stuck
         Given I have installed the "build-timeout" plugin
         And a job
         When I configure the job
         And I enable concurrent builds
         And I add a shell build step "sleep 1"
         And I save the job
         And I build 3 jobs
         And I wait for build to complete
         And I configure the job
         And I set the build timeout to likely stuck
         And I change a shell build step to "sleep 20"
         And I save the job
         And I build the job
         Then the build should fail
     */
    @Test
    public void fail_build_if_likely_stuck() {
        FreeStyleJob j = jenkins.jobs.create();
        j.configure();
        j.addShellStep("sleep 1");
        j.save();

        for (int i=0; i<3; i++)
            j.queueBuild().shouldSucceed();

        j.configure();
        {
            j.addShellStep("sleep 300");

            BuildTimeout t = new BuildTimeout(j);
            t.abortWhenStuck();
            t.writingDescription.check();
        }
        j.save();

        j.queueBuild().waitUntilFinished(300).shouldAbort();
    }

}
