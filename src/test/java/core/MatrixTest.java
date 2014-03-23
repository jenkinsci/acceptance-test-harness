package core;

import org.hamcrest.CoreMatchers;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.MatrixBuild;
import org.jenkinsci.test.acceptance.po.MatrixProject;
import org.jenkinsci.test.acceptance.po.MatrixRun;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 Feature: Use multi configuration job
   As a Jenkins user
   I want to configure and run multi configuration jobs
 */
public class MatrixTest extends AbstractJUnitTest {
    MatrixProject job;

    @Before
    public void setUp() {
        job = jenkins.jobs.create(MatrixProject.class);
    }

    /**
     Scenario: Run configurations sequentially
       Given a matrix job
       When I configure the job
       And I configure user axis "user_axis" with values "axis1 axis2 axis3"
       And I configure to run configurations sequentially
       And I add a shell build step "sleep 5"
       And I save the job
       And I build the job
       Then the configurations should run sequentially
     */
    @Test
    public void run_configurations_sequentially() {
        job.configure();
        job.addUserAxis("user_axis","axis1 axis2 axis3");
        job.runSequentially.check();
        job.addShellStep("sleep 5");
        job.save();

        MatrixBuild b = job.queueBuild().waitUntilStarted().as(MatrixBuild.class);
        assertThatBuildHasRunSequentially(b);
    }

    private void assertThatBuildHasRunSequentially(MatrixBuild b) {
        List<MatrixRun> builds = b.getConfigurations();

        while (b.isInProgress()) {
            int running = 0;
            for (MatrixRun r : builds) {
                if (r.isInProgress())
                    running++;
            }

            assertTrue("Too many configurations running at once", running<2);
            sleep(100);
        }
    }
}
