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
import static org.jenkinsci.test.acceptance.Matchers.hasContent;

/**
 * Verifies the Git repository setup by git-userContent plugin is working fine for some basic operations.
 */
@WithPlugins({"git-userContent", "git", "workflow-job", "workflow-cps", "workflow-basic-steps", "workflow-durable-task-step"})
public class GitUserContentTest extends AbstractJUnitTest {

    private static final String NEW_FILE_NAME = "text.txt";
    private static final String NEW_FILE_CONTENT = "anytextwhatever";

    private static final String GIT_FOLDER = ".git";
    private static final String README_FILE = "readme.txt";

    @Before
    public void setUp() throws Exception {
        final GlobalSecurityConfig security = new GlobalSecurityConfig(jenkins);
        security.open();
        security.csrf.uncheck();
        security.save();
    }

    @Test
    public void userContentGitTest() throws Exception {
        this.checkUserContent(README_FILE);

        this.createAndBuildWorkflowJob(String.format("node {\n" +
                "    git url: '%suserContent.git/'\n" +
                "    writeFile encoding: 'UTF-8', file: '%s', text: '%s'\n" +
                "    sh 'git add %s'\n" +
                "    sh 'git config user.email \"you@example.com\"'\n" +
                "    sh 'git config user.name \"Your Name\"'\n" +
                "    sh 'git commit -m \"Including new file\"'\n" +
                "    sh 'git push --set-upstream origin master'\n" +
                "}", jenkins.url, NEW_FILE_NAME, NEW_FILE_CONTENT, NEW_FILE_NAME));

        this.checkUserContent(README_FILE, GIT_FOLDER, NEW_FILE_NAME);

        final String pullConsole = this.createAndBuildWorkflowJob(String.format("node {\n" +
                "    git url: '%suserContent.git/'\n" +
                "    sh 'cat %s'\n" +
                "}", jenkins.url, NEW_FILE_NAME)).getConsole();

        assertThat(pullConsole, containsString("Cloning the remote Git repository"));
        assertThat(pullConsole, containsString(NEW_FILE_CONTENT));

        this.checkUserContent(README_FILE, GIT_FOLDER, NEW_FILE_NAME);
    }

    private Build createAndBuildWorkflowJob(final String script) {
        final WorkflowJob job = jenkins.jobs.create(WorkflowJob.class);
        job.script.set(script);
        job.save();
        return job.startBuild().shouldSucceed();
    }

    private void checkUserContent(String... expectedFiles) {
        jenkins.visit("userContent");

        for (final String file : expectedFiles) {
            assertThat(driver, hasContent(file));
        }
    }

}
