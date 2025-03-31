package plugins;

import static org.junit.Assert.fail;

import com.google.inject.Inject;
import hudson.plugins.jira.soap.RemoteComment;
import org.jenkinsci.test.acceptance.docker.DockerContainerHolder;
import org.jenkinsci.test.acceptance.docker.fixtures.JiraContainer;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.DockerTest;
import org.jenkinsci.test.acceptance.junit.Since;
import org.jenkinsci.test.acceptance.junit.WithCredentials;
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
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@WithPlugins({"jira", "git"})
@Category(DockerTest.class)
@WithDocker
@Since("2.73.3")
public class JiraPluginTest extends AbstractJUnitTest {
    @Inject
    DockerContainerHolder<JiraContainer> docker;

    GitRepo git;

    public static final String USERNAME = "admin";

    public static final String PASSWORD = "admin";

    public static final String CREDENTIALS_ID = "jira-admin";

    @Before
    public void setUp() throws Exception {
        git = new GitRepo();
        jenkins.runScript("hudson.plugins.git.GitSCM.ALLOW_LOCAL_CHECKOUT = true");
    }

    @After
    public void tearDown() throws Exception {
        if (git != null) {
            git.close();
        }

        jenkins.runScript("hudson.plugins.git.GitSCM.ALLOW_LOCAL_CHECKOUT = false");
    }

    @Test
    @WithCredentials(
            credentialType = WithCredentials.USERNAME_PASSWORD,
            values = {USERNAME, PASSWORD},
            id = CREDENTIALS_ID)
    @Ignore("Jira 6.3 no longer installs in the test container"
            + " https://github.com/jenkinsci/acceptance-test-harness/issues/1910")
    public void jira_ticket_gets_updated_with_a_build_link() throws Exception {
        JiraContainer jira = docker.get();
        jira.waitForReady(this);
        jira.createProject("ABC");
        jira.createIssue("ABC");
        jira.createIssue("ABC");

        jenkins.configure();
        {
            new JiraGlobalConfig(jenkins).addSite(jira.getURL(), CREDENTIALS_ID);
        }
        jenkins.save();

        FreeStyleJob job = jenkins.jobs.create();
        job.configure();
        {
            job.useScm(GitScm.class).url(git.dir.toString());
            job.addPublisher(JiraUpdater.class);
        }
        job.save();

        git.changeAndCommitFoo("initial commit");
        job.startBuild().shouldSucceed();

        git.changeAndCommitFoo("[ABC-1] fixed");
        git.changeAndCommitFoo("[ABC-2] fixed");
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
