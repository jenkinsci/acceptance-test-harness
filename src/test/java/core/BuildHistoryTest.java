package core;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.not;

import java.util.Set;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.BuildHistory;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.ListView;
import org.junit.Test;

public class BuildHistoryTest extends AbstractJUnitTest {

    @Test
    public void global_build_history() {
        FreeStyleJob job = jenkins.jobs.create();
        Build build = job.startBuild().waitUntilFinished();

        BuildHistory history = jenkins.getBuildHistory();
        assertThat(history.getBuilds(), contains(build));
        assertThat(history.getBuildsOf(job), contains(build));

        history = build.getNode().getBuildHistory();
        assertThat(history.getBuilds(), contains(build));
        assertThat(history.getBuildsOf(job), contains(build));
    }

    @Test
    public void view_build_history() {
        ListView view = jenkins.views.create(ListView.class, "a_view");

        FreeStyleJob inViewJob = view.jobs.create(FreeStyleJob.class, "in_view");
        Build inViewBuild = inViewJob.startBuild().waitUntilFinished();
        FreeStyleJob outOfViewJob = jenkins.jobs.create(FreeStyleJob.class, "not_in_view");
        Build outOfViewBuild = outOfViewJob.startBuild().waitUntilFinished();

        Set<Build> history = view.getBuildHistory().getBuilds();
        assertThat(history, contains(inViewBuild));
        assertThat(history, not(contains(outOfViewBuild)));
    }
}
