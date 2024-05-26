package plugins;

import jakarta.inject.Inject;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.GroupApi;
import org.gitlab4j.api.ProjectApi;
import org.gitlab4j.api.models.*;
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
import org.junit.Ignore;
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
    private String host;
    private int port;

    private String privateTokenAdmin;

    private String privateTokenUser;

    private String repoName = "testrepo";

    private String anotherRepoName = "anotherproject";

    private String adminUserName = "testadmin";

    private String userName = "testsimple";

    private String groupName = "firstgroup";

    private String JenkinsfileContent = "pipeline {\n" +
            "    agent any\n" +
            "\n" +
            "    stages {\n" +
            "        stage('Build') {\n" +
            "            steps {\n" +
            "                echo 'Building..'\n" +
            "            }\n" +
            "        }\n" +
            "        stage('Test') {\n" +
            "            steps {\n" +
            "                echo 'Testing..'\n" +
            "            }\n" +
            "        }\n" +
            "        stage('Deploy') {\n" +
            "            steps {\n" +
            "                echo 'Deploying....'\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "}";

    private String brokenJenkinsfile = "pipeline {\n" +
            "    agent any\n";

    String mainBranch = "main";

    String firstBranch = "firstbranch";
    String secondBranch = "secondbranch";
    String failedJob = "failedjob";

    public String getPrivateTokenAdmin() {
        return privateTokenAdmin;
    }

    public String getPrivateTokenUser() {
        return privateTokenUser;
    }
    @Before
    public void init() throws InterruptedException, IOException {
        container = gitLabServer.get();
        host = container.host();
        port = container.sshPort();
        container.waitForReady(this);

        // create an admin user
        privateTokenAdmin = container.createUserToken(adminUserName, "arandompassword12#", "testadmin@invalid.test", "true");

        // create another user
        privateTokenUser = container.createUserToken(userName, "passwordforsimpleuser12#", "testsimple@invalid.test", "false");
    }

    @Test
    @Ignore("https://github.com/jenkinsci/acceptance-test-harness/issues/1461")
    public void testGitLabMultibranchPipeline() throws IOException, GitLabApiException {
        createRepo();

        // initialize GitLabApi and Project
        GitLabApi gitlabapi = new GitLabApi(container.getHttpUrl().toString(), getPrivateTokenAdmin());
        ProjectApi projApi = new ProjectApi(gitlabapi);
        Project project = projApi.getProjects().stream().filter((proj -> repoName.equals(proj.getName()))).findAny().orElse(null);

        // create 2 new branches
        createBranch(gitlabapi, project, firstBranch, mainBranch);
        createBranch(gitlabapi, project, secondBranch, mainBranch);

        // a branch which is intended to fail
        createBranch(gitlabapi, project, failedJob, mainBranch);

        // add Jenkinsfile on these branches
        addFile(gitlabapi, project, mainBranch, JenkinsfileContent);
        addFile(gitlabapi, project, firstBranch, JenkinsfileContent);
        addFile(gitlabapi, project, secondBranch, JenkinsfileContent);

        // add a broken Jekinsfile to the failedjob
        addFile(gitlabapi, project, failedJob, brokenJenkinsfile);

        // create a merge request
        createMergeRequest(gitlabapi, project, secondBranch, mainBranch, "test_mr");

        createGitLabToken(privateTokenAdmin, "GitLab Personal Access Token");
        configureGitLabServer();

        final WorkflowMultiBranchJob multibranchJob = jenkins.jobs.create(WorkflowMultiBranchJob.class);
        this.configureJobWithGitLabBranchSource(multibranchJob, adminUserName, repoName);
        multibranchJob.save();

        multibranchJob.waitForBranchIndexingFinished((int)time.seconds(20));
        this.assertBranchIndexing(multibranchJob);

        final WorkflowJob successJob1 = multibranchJob.getJob(mainBranch);
        final WorkflowJob successJob2 = multibranchJob.getJob(firstBranch);
        final WorkflowJob successJob3 = multibranchJob.getJob("MR-1");
        final WorkflowJob failureJob = multibranchJob.getJob(failedJob);

        this.assertExistAndResult(successJob1, true);
        this.assertExistAndResult(successJob2, true);
        this.assertExistAndResult(successJob3, true);
        this.assertExistAndResult(failureJob, false);

        // delete the repo when finished
        container.deleteRepo(getPrivateTokenAdmin(), repoName);
    }

    // TODO: re-enable when flaky tests have been resolved. see: https://github.com/jenkinsci/acceptance-test-harness/pull/1365
    @Ignore("flaky test")
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

        // test the builds for the first project
        final WorkflowMultiBranchJob first_project = organizationFolder.getJobs().get(WorkflowMultiBranchJob.class, groupName+"%2F"+repoName);
        checksBuildsWithinProject(first_project);

        // test the builds for the second project
        final WorkflowMultiBranchJob second_project = organizationFolder.getJobs().get(WorkflowMultiBranchJob.class, groupName+"%2F"+anotherRepoName);
        checksBuildsWithinProject(second_project);
    }

    public void createRepo() throws RuntimeException {
        //This sends a request to make a new repo in the gitlab server with the name "testrepo"
        HttpResponse<String> response = container.createRepo(repoName, getPrivateTokenAdmin());
        assertEquals(201, response.statusCode()); // 201 means the repo was created successfully
    }

    private void createBranch(GitLabApi gitlabapi, Project project, String branchName, String sourceBranch) throws GitLabApiException {
        gitlabapi.getRepositoryApi().createBranch(project.getId(), branchName, sourceBranch);
    }

    private void addFile(GitLabApi gitlabapi, Project project, String branchName, String JenkinsfileContent) throws GitLabApiException {
        RepositoryFile file = new RepositoryFile();
        file.setFileName("Jenkinsfile");
        file.setFilePath("Jenkinsfile");
        file.setContent(JenkinsfileContent);

        // create Jenkinsfile on the main branch
        gitlabapi.getRepositoryFileApi().createFile(project.getId(), file, branchName, "Add Jenkinsfile");
    }

    private void createMergeRequest(GitLabApi gitlabapi, Project project, String sourceBranch, String targetBranch, String mrTitle) throws GitLabApiException {
        MergeRequestParams params = new MergeRequestParams()
                .withSourceBranch(sourceBranch)
                .withTargetBranch(targetBranch)
                .withTitle(mrTitle);
        gitlabapi.getMergeRequestApi().createMergeRequest(project, params);
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
        waitFor(job.getLastBuild().getResult(), Matchers.containsString(expectedResult.name()), (int)time.seconds(3));
    }

    private void checksBuildsWithinProject(WorkflowMultiBranchJob project) {
        final WorkflowJob successJob1 = project.getJob("main");
        final WorkflowJob successJob2 = project.getJob("firstbranch");
        final WorkflowJob successJob3 = project.getJob("MR-1");
        final WorkflowJob failureJob = project.getJob("failedjob");

        this.assertExistAndResult(successJob1, true);
        this.assertExistAndResult(successJob2, true);
        this.assertExistAndResult(successJob3, true);
        this.assertExistAndResult(failureJob, false);
    }

    private void createGroup() throws GitLabApiException, IOException {
        GitLabApi gitlabapi = new GitLabApi(container.getHttpUrl().toString(), privateTokenAdmin);
        GroupApi groupApi = new GroupApi(gitlabapi);
        GroupParams groupParams = new GroupParams().withName(groupName).withPath(groupName).withMembershipLock(false);
        Group group = groupApi.createGroup(groupParams).withVisibility(Visibility.PRIVATE);
        groupApi.addMember(group.getId(), gitlabapi.getUserApi().getOptionalUser(userName).get().getId(), AccessLevel.DEVELOPER);

        // create a project in the group
        Project project = new Project().withPublic(false)
                .withPath(repoName)
                .withNamespaceId(group.getId());
        ProjectApi projApi = new ProjectApi(gitlabapi);
        projApi.createProject(project);

        project = projApi.getProjects().stream().filter((proj -> repoName.equals(proj.getName()))).findAny().orElse(null);

        // populate the repository
        createBranch(gitlabapi, project, firstBranch, mainBranch);
        createBranch(gitlabapi, project, secondBranch, mainBranch);
        createBranch(gitlabapi, project, failedJob, mainBranch);

        addFile(gitlabapi, project, mainBranch, JenkinsfileContent);
        addFile(gitlabapi, project, firstBranch, JenkinsfileContent);
        addFile(gitlabapi, project, secondBranch, JenkinsfileContent);
        addFile(gitlabapi, project, failedJob, brokenJenkinsfile);

        createMergeRequest(gitlabapi, project, secondBranch, mainBranch, "test_mr");

        // create another project within the group
        project = new Project().withPublic(false)
                .withPath(anotherRepoName)
                .withNamespaceId(group.getId());

        projApi.createProject(project);

        project = projApi.getProjects().stream().filter((proj -> anotherRepoName.equals(proj.getName()))).findAny().orElse(null);

        // populate the repository
        createBranch(gitlabapi, project, firstBranch, mainBranch);
        createBranch(gitlabapi, project, secondBranch, mainBranch);
        createBranch(gitlabapi, project, failedJob, mainBranch);

        addFile(gitlabapi, project, mainBranch, JenkinsfileContent);
        addFile(gitlabapi, project, firstBranch, JenkinsfileContent);
        addFile(gitlabapi, project, secondBranch, JenkinsfileContent);
        addFile(gitlabapi, project, failedJob, brokenJenkinsfile);

        createMergeRequest(gitlabapi, project, secondBranch, mainBranch, "test_mr");
    }

    private void assertCheckFinishedSuccessfully(final GitLabOrganizationFolder job) {
        assertThat(driver, hasContent("Scan GitLab Group Now"));
        final String branchIndexingLog = job.getCheckLog();

        assertThat(branchIndexingLog, containsString("Finished: SUCCESS"));
    }
}
