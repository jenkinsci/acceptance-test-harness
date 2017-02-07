package plugins;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.GlobalSecurityConfig;
import org.jenkinsci.test.acceptance.po.WorkflowJob;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.containsString;

/**
 * Verifies the Git repository setup by git-userContent plugin is working fine for some basic operations.
 */
@WithPlugins({"git-userContent", "git", "workflow-job", "workflow-cps", "workflow-basic-steps", "workflow-durable-task-step"})
public class GitUserContentTest extends AbstractJUnitTest {

    private static final String FILE_NAME = "text.txt";
    private static final String FILE_CONTENT = "anytextwhatever";

    @Before
    public void setUp() throws Exception {
        final GlobalSecurityConfig security = new GlobalSecurityConfig(jenkins);
        security.open();
        security.csrf.uncheck();
        security.save();
    }

    @Test
    public void userContentGitTest() throws Exception {
        this.createAndBuildWorkflowJob(String.format("node {\n" +
                "    git url: '%suserContent.git/'\n" +
                "    writeFile encoding: 'UTF-8', file: '%s', text: '%s'\n" +
                "    sh 'git add %s'\n" +
                "    sh 'git commit -m \"Including new file\"'\n" +
                "    sh 'git push --set-upstream origin master'\n" +
                "}", jenkins.url, FILE_NAME, FILE_CONTENT, FILE_NAME));

        final String pullConsole = this.createAndBuildWorkflowJob(String.format("node {\n" +
                "    git url: '%suserContent.git/'\n" +
                "    sh 'cat %s'\n" +
                "}", jenkins.url, FILE_NAME)).getConsole();

        assertThat(pullConsole, containsString("Cloning the remote Git repository"));
        assertThat(pullConsole, containsString(FILE_CONTENT));
    }

    private Build createAndBuildWorkflowJob(final String script) {
        final WorkflowJob job = jenkins.jobs.create(WorkflowJob.class);
        job.script.set(script);
        job.save();
        return job.startBuild().shouldSucceed();
    }

}
