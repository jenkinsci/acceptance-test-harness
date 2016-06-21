package plugins;

import com.google.inject.Inject;

import hudson.plugins.jira.soap.RemoteComment;

import org.jenkinsci.test.acceptance.docker.DockerContainerHolder;
import org.jenkinsci.test.acceptance.docker.fixtures.JiraContainer;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.DockerTest;
import org.jenkinsci.test.acceptance.junit.WithDocker;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.git.GitRepo;
import org.jenkinsci.test.acceptance.plugins.git.GitScm;
import org.jenkinsci.test.acceptance.plugins.jira.JiraGlobalConfig;
import org.jenkinsci.test.acceptance.plugins.jira.JiraUpdater;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.fail;

/**
 * Feature: Update JIRA tickets when a build is ready
 * In order to notify people waiting for a bug fix
 * As a Jenkins developer
 * I want JIRA issues to be updated when a new build is made
 */
@WithPlugins({"jira", "git"})
@Category(DockerTest.class)
@WithDocker
public class JiraPluginTest extends AbstractJUnitTest {
    @Inject
    DockerContainerHolder<JiraContainer> docker;

    GitRepo git;

    @Before
    public void setUp() throws Exception {
        git = new GitRepo();
    }

    @After
    public void tearDown() throws Exception {
        if (git!=null)
            git.close();
    }

    /**
     * @native(docker) Scenario: JIRA ticket gets updated with a build link
     * Given a docker fixture "jira"
     * And "ABC" project on docker jira fixture
     * And a new issue in "ABC" project on docker jira fixture
     * And a new issue in "ABC" project on docker jira fixture
     * And I have installed the "jira" plugin
     * <p/>
     * Given I have installed the "git" plugin
     * And an empty test git repository
     * And a job
     * <p/>
     * Then I configure docker fixture as JIRA site
     * <p/>
     * Then I configure the job
     * And I check out code from the test Git repository
     * And I add "Update relevant JIRA issues" post-build action
     * And I save the job
     * <p/>
     * Then I commit "initial commit" to the test Git repository
     * And I build the job
     * And the build should succeed
     * <p/>
     * When I commit "[ABC-1] fixed" to the test Git repository
     * And I commit "[ABC-2] fixed" to the test Git repository
     * And I build the job
     * Then the build should succeed
     * And the build should link to JIRA ABC-1 ticket
     * And the build should link to JIRA ABC-2 ticket
     * And JIRA ABC-1 ticket has comment from admin that refers to the build
     */
    @Test
    public void jira_ticket_gets_updated_with_a_build_link() throws Exception {
        JiraContainer jira = docker.get();
        jira.waitForReady(this);
        jira.createProject("ABC");
        jira.createIssue("ABC");
        jira.createIssue("ABC");

        jenkins.configure();
        {
            new JiraGlobalConfig(jenkins).addSite(jira.getURL(), "admin", "admin");
        }
        jenkins.save();

        FreeStyleJob job = jenkins.jobs.create();
        job.configure();
        {
            job.useScm(GitScm.class).url(git.dir.toString());
            job.addPublisher(JiraUpdater.class);
        }
        job.save();

        git.commit("initial commit");
        job.startBuild().shouldSucceed();

        git.commit("[ABC-1] fixed");
        git.commit("[ABC-2] fixed");
        Build b = job.startBuild().shouldSucceed();

        b.open();
        find(by.link("ABC-1"));
        find(by.link("ABC-2"));

        String buildUrl = job.build(b.getNumber()).url.toString();
        for (RemoteComment c : jira.getComments("ABC-1")) {
            if (c.getBody().contains(buildUrl)) {
                return;
            }
        }
        fail("Comment back to Jenkins not found");
    }
}
