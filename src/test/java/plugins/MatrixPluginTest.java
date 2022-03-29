package plugins;

import com.google.inject.Inject;
import hudson.util.VersionNumber;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jvnet.hudson.test.Issue;
import org.jenkinsci.test.acceptance.junit.Since;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.po.*;
import org.jenkinsci.test.acceptance.slave.SlaveProvider;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static java.util.Collections.singletonMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;

@WithPlugins("matrix-project")
public class MatrixPluginTest extends AbstractJUnitTest {
    MatrixProject job;

    @Inject
    SlaveProvider slaves;

    @Before
    public void setUp() {
        job = jenkins.jobs.create(MatrixProject.class);
    }

    @Test
    public void run_configurations_sequentially() {
        job.configure();
        job.addUserAxis("user_axis", "axis1 axis2 axis3");
        job.runSequentially.check();
        job.addShellStep("sleep 5");
        job.save();

        MatrixBuild b = job.startBuild().waitUntilStarted().as(MatrixBuild.class);
        assertThatBuildHasRunSequentially(b);
    }

    @Test
    public void run_a_matrix_job() {
        job.configure();
        job.addUserAxis("user_axis", "axis1 axis2 axis3");
        job.addShellStep("ls");
        job.save();
        job.startBuild().shouldSucceed();
        for (MatrixConfiguration c : job.getConfigurations()) {
            c.getLastBuild().shouldContainsConsoleOutput("\\+ ls");
        }
    }

    @Test
    public void run_touchstone_builds_first_with_result_stable() {
        job.configure();
        job.addUserAxis("user_axis", "axis1 axis2 axis3");
        job.addShellStep("false");
        job.setTouchStoneBuild("user_axis=='axis3'", "UNSTABLE");
        job.save();
        MatrixBuild b = job.startBuild().waitUntilFinished().as(MatrixBuild.class);

        b.getConfiguration("user_axis=axis1").shouldNotExist();
        b.getConfiguration("user_axis=axis2").shouldNotExist();
        b.getConfiguration("user_axis=axis3").shouldExist();
    }

    @Test
    public void run_build_with_combination_filter() {
        job.configure();
        job.addUserAxis("user_axis", "axis1 axis2 axis3");
        job.setCombinationFilter("user_axis=='axis2'");
        job.addShellStep("echo hello");
        job.save();

        MatrixBuild b = job.startBuild().waitUntilFinished().as(MatrixBuild.class);

        b.getConfiguration("user_axis=axis1").shouldNotExist();
        b.getConfiguration("user_axis=axis2").shouldExist();
        b.getConfiguration("user_axis=axis3").shouldNotExist();
    }

    @Test
    @Issue("JENKINS-7285")
    @Since("1.515")
    public void use_job_parameters_in_combination_filters() {
        job.configure();
        job.addUserAxis("run", "yes maybe no");
        job.setCombinationFilter("run=='yes' || (run=='maybe' && condition=='true')");
        job.addParameter(StringParameter.class).setName("condition");
        job.save();

        MatrixBuild b = job.startBuild(singletonMap("condition", "false")).waitUntilFinished().as(MatrixBuild.class);
        b.getConfiguration("run=yes").shouldExist();
        b.getConfiguration("run=maybe").shouldNotExist();
        b.getConfiguration("run=no").shouldNotExist();

        b = job.startBuild(singletonMap("condition", "true")).waitUntilFinished().as(MatrixBuild.class);
        b.getConfiguration("run=yes").shouldExist();
        b.getConfiguration("run=maybe").shouldExist();
        b.getConfiguration("run=no").shouldNotExist();
    }

    @Test
    public void run_configurations_on_with_a_given_label() throws Exception {
        Slave s = slaves.get().install(jenkins).get();
        s.configure();
        s.setLabels("label1");
        s.save();

        job.configure();
        LabelAxis a = job.addAxis(LabelAxis.class);
        String builtInNodeName;
        String builtInNodeDescription;
        if (jenkins.getVersion().isOlderThan(new VersionNumber("2.307"))) {
            builtInNodeName = "master";
            builtInNodeDescription = "master";
        } else {
            builtInNodeName = "built-in";
            builtInNodeDescription = "the built-in node";
        }
        a.select(builtInNodeName);
        a.select("label1");
        job.save();

        MatrixBuild b = job.startBuild().waitUntilFinished().as(MatrixBuild.class);
        b.getConfiguration("label=" + builtInNodeName).shouldContainsConsoleOutput("(Building|Building remotely) on " + builtInNodeDescription);
        b.getConfiguration("label=label1").shouldContainsConsoleOutput("(Building|Building remotely) on " + s.getName());
    }

    private void assertThatBuildHasRunSequentially(MatrixBuild b) {
        List<MatrixRun> builds = b.getConfigurations();

        while (b.isInProgress()) {
            int running = 0;
            for (MatrixRun r : builds) {
                if (r.isInProgress()) {
                    running++;
                }
            }

            assertThat("Too many configurations running at once", running, is(lessThan(2)));
            sleep(100);
        }
    }
}
