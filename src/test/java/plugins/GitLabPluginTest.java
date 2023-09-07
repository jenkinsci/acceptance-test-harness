package plugins;

import jakarta.inject.Inject;
import org.gitlab4j.api.GitLabApiException;
import org.jenkinsci.test.acceptance.Matchers;
import org.jenkinsci.test.acceptance.docker.DockerContainerHolder;
import org.jenkinsci.test.acceptance.docker.fixtures.GitLabContainer;
import org.jenkinsci.test.acceptance.junit.*;
import org.jenkinsci.test.acceptance.plugins.credentials.CredentialsPage;
import org.jenkinsci.test.acceptance.plugins.credentials.ManagedCredentials;
import org.jenkinsci.test.acceptance.plugins.gitlab_plugin.*;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.WorkflowJob;
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

    private String privateTokenAdmin;

    private String privateTokenUser;

    private String repoName = "testrepo";

    private String adminUserName = "testadmin";

    private String userName = "testsimple";

    private String groupName = "firstgroup";

    public String getPrivateTokenAdmin() {
        return privateTokenAdmin;
    }

    public String getPrivateTokenUser() {
        return privateTokenUser;
    }
    @Before
    public void init() throws InterruptedException, IOException {
        container = gitLabServer.get();
        repoUrl = container.getRepoUrl();
        host = container.host();
        port = container.port();
        container.waitForReady(this);

        // create an admin user
        privateTokenAdmin = container.createUserToken(adminUserName, "arandompassword12#", "testadmin@example.com", "true");

        // create another user
        privateTokenUser = container.createUserToken(userName, "passwordforsimpleuser12#", "testsimple@example.com", "false");
    }

    @Test
    public void dummy_test() {
        assertNotNull(container.getRepoUrl());
        assertTrue(container.getRepoUrl().contains("ssh://git@"));
        assertNotNull(container.host());
    }

    public void createRepo() throws IOException, GitLabApiException {
        //This sends a request to make a new repo in the gitlab server with the name "testrepo"
        HttpResponse<String> response = container.createRepo(repoName, getPrivateTokenAdmin());
        assertEquals(201, response.statusCode()); // 201 means the repo was created successfully
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
        createGitLabToken(privateTokenAdmin, "GitLab Personal Access Token");
        configureGitLabServer();
        createRepo();
        container.createBranch(getPrivateTokenAdmin(), repoName);

        final WorkflowMultiBranchJob multibranchJob = jenkins.jobs.create(WorkflowMultiBranchJob.class);
        this.configureJobWithGitLabBranchSource(multibranchJob, adminUserName, repoName);
        multibranchJob.save();

        multibranchJob.waitForBranchIndexingFinished((int)time.seconds(20));
        this.assertBranchIndexing(multibranchJob);

        final WorkflowJob successJob1 = multibranchJob.getJob("main");
        final WorkflowJob successJob2 = multibranchJob.getJob("firstbranch");
        final WorkflowJob successJob3 = multibranchJob.getJob("MR-1");
        final WorkflowJob failureJob = multibranchJob.getJob("failedjob");

        this.assertExistAndResult(successJob1, true);
        this.assertExistAndResult(successJob2, true);
        this.assertExistAndResult(successJob3, true);
        this.assertExistAndResult(failureJob, false);

        // delete the repo when finished
        container.deleteRepo(getPrivateTokenAdmin(), repoName);
    }

    private void assertBranchIndexing(final WorkflowMultiBranchJob job) {
        assertThat(driver, hasContent("Scan GitLab Project Now"));
        final String branchIndexingLog = job.getBranchIndexingLog();

        assertThat(branchIndexingLog, containsString("Scheduled build for branch: main"));
        assertThat(branchIndexingLog, containsString("Scheduled build for branch: firstbranch"));
        assertThat(branchIndexingLog, containsString("Scheduled build for branch: failedjob"));
        assertThat(branchIndexingLog, containsString("Scheduled build for branch: MR-1"));
        assertThat(branchIndexingLog, containsString("1 merge requests were processed"));
    }

    private void configureJobWithGitLabBranchSource(final WorkflowMultiBranchJob job, String adminUserName, String project) {
        final GitLabBranchSource ghBranchSource = job.addBranchSource(GitLabBranchSource.class);
        ghBranchSource.setOwner(adminUserName);
        ghBranchSource.setProject(adminUserName, project);
    }

    private void assertExistAndResult(final WorkflowJob job, final boolean withSuccess) {
        final Build.Result expectedResult = (withSuccess) ? Build.Result.SUCCESS : Build.Result.FAILURE;
        waitFor(job, Matchers.pageObjectExists(), (int)time.seconds(3));
        assertEquals(job.getLastBuild().getResult(), expectedResult.name());
    }

    @Test
    public void gitLabGroupFolderOrganization() throws GitLabApiException, IOException {
        createGroup();
        createGitLabToken(privateTokenAdmin, "GitLab Personal Access Token");
        configureGitLabServer();

        final GitLabOrganizationFolder organizationFolder = jenkins.jobs.create(GitLabOrganizationFolder.class);
        organizationFolder.create(groupName);
        organizationFolder.save();

        // test the pipeline
        organizationFolder.waitForCheckFinished((int)time.seconds(20));

        this.assertCheckFinishedSuccessfully(organizationFolder);

        final GitLabOrganizationFolder successJob1 = organizationFolder.getJob("main");

    }

    private void createGroup() throws GitLabApiException, IOException {
        container.createGroup(groupName, userName, privateTokenAdmin, repoName);
    }

    private void assertCheckFinishedSuccessfully(final GitLabOrganizationFolder job) {
        assertThat(driver, hasContent("Scan GitLab Group Now"));
        final String branchIndexingLog = job.getCheckLog();

        assertThat(branchIndexingLog, containsString("Finished: SUCCESS"));
    }
}
