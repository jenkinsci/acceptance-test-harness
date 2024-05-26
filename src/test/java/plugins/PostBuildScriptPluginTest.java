package plugins;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;

import org.jenkinsci.test.acceptance.Matcher;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.post_build_script.PostBuildScript;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.ShellBuildStep;
import org.junit.Before;
import org.junit.Test;

@WithPlugins({
        "matrix-project", // JENKINS-37545
        "postbuildscript"
})
public class PostBuildScriptPluginTest extends AbstractJUnitTest {

    private FreeStyleJob j;

    @Before
    public void setUp() {
        j = jenkins.jobs.create();
    }

    @Test
    public void run_post_build_step() {
        j.configure();
        addMarkerPostBuildStep();
        j.save();

        Build build = j.startBuild().shouldSucceed();

        assertThat(build, runPostBuildStep());
    }

    @Test
    public void skip_for_jobs_that_succeeded() {
        j.configure();
        addMarkerPostBuildStep().runWhenFailed();
        j.save();

        Build build = j.startBuild().shouldSucceed();

        assertThat(build, not(runPostBuildStep()));
    }

    private PostBuildScript addMarkerPostBuildStep() {
        PostBuildScript post = j.addPublisher(PostBuildScript.class);
        j.control("/publisher[PostBuildScript]/repeatable-add[3]").click();
        post.addStep(ShellBuildStep.class).command("echo RUNNING_POST_BUILD_STEP");

        return post;
    }

    private Matcher<Build> runPostBuildStep() {
        return new Matcher<Build>("post build step was run") {
            @Override
            public boolean matchesSafely(Build item) {
                return item.getConsole().contains("RUNNING_POST_BUILD_STEP");
            }
        };
    }
}
