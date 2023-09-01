package plugins;

import jakarta.inject.Inject;
import org.gitlab4j.api.GitLabApiException;
import org.jenkinsci.test.acceptance.docker.DockerContainerHolder;
import org.jenkinsci.test.acceptance.docker.fixtures.GitLabContainer;
import org.jenkinsci.test.acceptance.junit.*;
import org.jenkinsci.test.acceptance.plugins.credentials.CredentialsPage;
import org.jenkinsci.test.acceptance.plugins.credentials.ManagedCredentials;
import org.jenkinsci.test.acceptance.plugins.gitlab_plugin.*;
import org.jenkinsci.test.acceptance.po.WorkflowMultiBranchJob;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.net.http.HttpResponse;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.jenkinsci.test.acceptance.Matchers.hasContent;
import static org.junit.Assert.*;

@WithDocker
@Category(DockerTest.class)
@WithPlugins({"gitlab-branch-source", "workflow-multibranch"})
public class GitLabPluginTest extends AbstractJUnitTest {

    @Inject
    DockerContainerHolder<GitLabContainer> gitLabServer;

    private GitLabContainer container;
    private String repoUrl;
    private String host;
    private int port;

    private String privateToken;

    private String password = "arandompassword12#"; // I guess the password can be the same for all users

    private String repoName = "testrepo";

    private String adminUserName = "testadmin";

    private String userName = "testsimple";

    public String getPrivateToken() {
        return privateToken;
    }

    @Before
    public void init() throws InterruptedException, IOException {
        container = gitLabServer.get();
        repoUrl = container.getRepoUrl();
        host = container.host();
        port = container.port();
        container.waitForReady(this);

        // create an admin user
        privateToken = container.createUserToken(adminUserName, password, "testadmin@gmail.com", "true");

        // create another user
        privateToken = container.createUserToken(userName, password, "testsimple@gmail.com", "false");
    }

    @Test
    public void dummy_test() {
        assertNotNull(container.getRepoUrl());
        assertTrue(container.getRepoUrl().contains("ssh://git@"));
        assertNotNull(container.host());
    }

    @Test
    public void createRepo() throws IOException, GitLabApiException {
        //This sends a request to make a new repo in the gitlab server with the name "testrepo"
        HttpResponse<String> response = container.createRepo(repoName, getPrivateToken());
        assertEquals(201, response.statusCode()); // 201 means the repo was created successfully

        // delete the repo when finished
        container.deleteRepo(getPrivateToken(), repoName);
    }

    public void createGitLabToken(String token, String id) {
        CredentialsPage cp = new CredentialsPage(jenkins, ManagedCredentials.DEFAULT_DOMAIN);
        cp.open();

        GitLabPersonalAccessTokenCredential tk = cp.add(GitLabPersonalAccessTokenCredential.class);
        tk.setToken(token);
        tk.setId(id);
        tk.create();
    }

    public void configureGitLabServer() throws IOException {
        jenkins.configure();

        // server configuration
        GitLabServerConfig serverConfig = new GitLabServerConfig(jenkins);
        serverConfig.configureServer(container.getHttpUrl().toString());
        jenkins.save();
    }

    @Test
    public void testGitLabMultibranchPipeline() throws IOException, GitLabApiException {
        createGitLabToken(privateToken, "GitLab Personal Access Token");
        configureGitLabServer();
        createRepo();

        final WorkflowMultiBranchJob multibranchJob = jenkins.jobs.create(WorkflowMultiBranchJob.class);
        this.configureJobWithGitLabBranchSource(multibranchJob, adminUserName, repoName);
        multibranchJob.save();
        multibranchJob.waitForBranchIndexingFinished(20);

        this.assertBranchIndexing(multibranchJob);

    }

    private void assertBranchIndexing(final WorkflowMultiBranchJob job) {
        assertThat(driver, hasContent("Scan GitLab Project Now"));
        final String branchIndexingLog = job.getBranchIndexingLog();

        assertThat(branchIndexingLog, containsString("Scheduled build for branch: main"));
        assertThat(branchIndexingLog, containsString("Scheduled build for branch: newnewbr"));
        assertThat(branchIndexingLog, containsString("Scheduled build for branch: MR-1"));
        assertThat(branchIndexingLog, containsString("1 merge requests were processed"));
    }

    private void configureJobWithGitLabBranchSource(final WorkflowMultiBranchJob job, String userName, String project) {
        final GitLabBranchSource ghBranchSource = job.addBranchSource(GitLabBranchSource.class);
        ghBranchSource.setOwner(adminUserName);
        ghBranchSource.setProject(adminUserName, project);
    }
}
