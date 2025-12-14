package plugins;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.jenkinsci.test.acceptance.docker.fixtures.GitLabContainer.GITLAB_API_CONNECT_TIMEOUT_MS;
import static org.jenkinsci.test.acceptance.docker.fixtures.GitLabContainer.GITLAB_API_READ_TIMEOUT_MS;

import jakarta.inject.Inject;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.GroupApi;
import org.gitlab4j.api.ProjectApi;
import org.gitlab4j.api.models.AccessLevel;
import org.gitlab4j.api.models.Group;
import org.gitlab4j.api.models.GroupParams;
import org.gitlab4j.api.models.Member;
import org.gitlab4j.api.models.MergeRequest;
import org.gitlab4j.api.models.MergeRequestFilter;
import org.gitlab4j.api.models.MergeRequestParams;
import org.gitlab4j.api.models.Project;
import org.gitlab4j.api.models.Tag;
import org.gitlab4j.api.models.Visibility;
import org.jenkinsci.test.acceptance.docker.DockerContainerHolder;
import org.jenkinsci.test.acceptance.docker.fixtures.GitLabContainer;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.DockerTest;
import org.jenkinsci.test.acceptance.junit.WithDocker;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.credentials.CredentialsPage;
import org.jenkinsci.test.acceptance.plugins.credentials.ManagedCredentials;
import org.jenkinsci.test.acceptance.plugins.git.GitRepo;
import org.jenkinsci.test.acceptance.plugins.gitlab_plugin.GitLabBranchSource;
import org.jenkinsci.test.acceptance.plugins.gitlab_plugin.GitLabOrganizationFolder;
import org.jenkinsci.test.acceptance.plugins.gitlab_plugin.GitLabPersonalAccessTokenCredential;
import org.jenkinsci.test.acceptance.plugins.gitlab_plugin.GitLabServerConfig;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.WorkflowJob;
import org.jenkinsci.test.acceptance.po.WorkflowMultiBranchJob;
import org.jenkinsci.utils.process.CommandBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

/**
 * Tests GitLab Branch Source plugin integration using a local GitLab CE container.
 */
@WithDocker
@Category(DockerTest.class)
@WithPlugins({"gitlab-branch-source", "workflow-multibranch"})
public class GitLabPluginTest extends AbstractJUnitTest {

    @Inject
    DockerContainerHolder<GitLabContainer> gitLabServer;

    private GitLabContainer container;

    private static final Duration BRANCH_INDEXING_TIMEOUT = Duration.ofMinutes(2);
    private static final Duration BUILD_COMPLETION_TIMEOUT = Duration.ofMinutes(2);
    private static final Duration JOB_CREATION_TIMEOUT = Duration.ofSeconds(30);
    private static final Duration GITLAB_API_RETRY_TIMEOUT = Duration.ofSeconds(30);
    private static final Duration POLLING_INTERVAL = Duration.ofSeconds(10);
    private static final Duration PAGE_LOAD_TIMEOUT = Duration.ofSeconds(10);

    private static final String REPO_NAME = "testrepo";
    private static final String ANOTHER_REPO_NAME = "anotherproject";
    private static final String ADMIN_USERNAME = "testadmin";
    private static final String USERNAME = "testsimple";
    private static final String GROUP_NAME = "firstgroup";

    private static final String MAIN_BRANCH = "main";
    private static final String FIRST_BRANCH = "firstbranch";
    private static final String SECOND_BRANCH = "secondbranch";
    private static final String FAILED_JOB = "failedjob";

    private static final String JENKINSFILE_CONTENT = """
            pipeline {
                agent any
                stages {
                    stage('Build') {
                        steps {
                            echo 'Building..'
                        }
                    }
                    stage('Test') {
                        steps {
                            echo 'Testing..'
                        }
                    }
                    stage('Deploy') {
                        steps {
                            echo 'Deploying....'
                        }
                    }
                }
            }
            """;

    private static final String BROKEN_JENKINSFILE = """
            pipeline {
                agent any
            """;

    private String adminToken;
    private String userToken;

    @Before
    public void init() throws InterruptedException, IOException {
        Instant startTime = Instant.now();

        // Keep WebDriver session alive during GitLab startup to prevent timeout
        Thread keepaliveThread = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    Thread.sleep(30000);
                    try {
                        driver.getCurrentUrl();
                    } catch (Exception e) {
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        keepaliveThread.setDaemon(true);
        keepaliveThread.start();

        try {
            jenkins.open();

            var starter = gitLabServer.starter();
            // https://docs.gitlab.com/ee/install/requirements.html#memory
            starter.withOptions(new CommandBuilder()
                    .add("--shm-size", "1g")
                    .add("--memory", "4g")
                    .add("--memory-swap", "5g"));
            container = starter.start();
            container.waitForReady(this);
            adminToken =
                    container.createUserToken(ADMIN_USERNAME, "arandompassword12#", "testadmin@invalid.test", "true");
            userToken =
                    container.createUserToken(USERNAME, "passwordforsimpleuser12#", "testsimple@invalid.test", "false");
            System.out.println("GitLab container init: " + elapsed(startTime));
        } finally {
            keepaliveThread.interrupt();
        }
    }

    @After
    public void cleanup() throws IOException {
        if (container != null) {
            container.cleanup(adminToken, REPO_NAME, GROUP_NAME);
            container.close();
            container = null;
        }
    }

    /**
     * Verifies multibranch pipeline discovers branches/MRs/tags, builds them, and detects dynamic branch
     */
    @Test
    public void testGitLabMultibranchPipeline() throws IOException, GitLabApiException {
        // Given a repository with 4 branches (3 valid, 1 broken) and 1 merge request
        Project project;
        try (var gitlabapi = new GitLabApi(container.getHttpUrl(), userToken)
                .withRequestTimeout(GITLAB_API_CONNECT_TIMEOUT_MS, GITLAB_API_READ_TIMEOUT_MS)) {
            project = createProjectViaApi(
                    gitlabapi.getProjectApi(), new Project().withName(REPO_NAME).withInitializeWithReadme(true));

            String gitlabRepoUrl = container.repoUrl(USERNAME + "/" + REPO_NAME, userToken);
            setUpInitialBranchViaGit(gitlabRepoUrl, MAIN_BRANCH, JENKINSFILE_CONTENT);
            setupBranchFromViaGit(gitlabRepoUrl, FIRST_BRANCH, MAIN_BRANCH, JENKINSFILE_CONTENT);
            setupBranchFromViaGit(gitlabRepoUrl, SECOND_BRANCH, MAIN_BRANCH, JENKINSFILE_CONTENT);
            setupBranchFromViaGit(gitlabRepoUrl, FAILED_JOB, MAIN_BRANCH, BROKEN_JENKINSFILE);

            MergeRequest mr = createMergeRequestViaApi(gitlabapi, project, SECOND_BRANCH, MAIN_BRANCH, "test_mr");
            awaitMergeRequestMergeRefViaApi(gitlabapi, project.getId(), mr.getIid());
        }

        createGitLabToken(userToken, "GitLab Personal Access Token");
        configureGitLabServer();

        // When multibranch job scans GitLab project
        final WorkflowMultiBranchJob multibranchJob = jenkins.jobs.create(WorkflowMultiBranchJob.class);
        configureJobWithGitLabBranchSource(multibranchJob, USERNAME, REPO_NAME);
        multibranchJob.save();

        multibranchJob.waitForBranchIndexingFinished((int) BRANCH_INDEXING_TIMEOUT.toSeconds());

        // Then all branches and MR discovered, valid builds succeed, broken build fails
        var log = multibranchJob.getBranchIndexingLogText();
        assertThat(log, containsString("Finished: SUCCESS"));
        assertThat(log, containsString("Scheduled build for branch: main"));
        assertThat(log, containsString("Scheduled build for branch: firstbranch"));
        assertThat(log, containsString("Scheduled build for branch: failedjob"));
        assertThat(log, containsString("Scheduled build for branch: MR-1"));
        assertThat(log, containsString("1 merge requests were processed"));

        assertExistAndResult(multibranchJob.getJob(MAIN_BRANCH), true);
        assertExistAndResult(multibranchJob.getJob(FIRST_BRANCH), true);
        assertExistAndResult(multibranchJob.getJob("MR-1"), true);
        assertExistAndResult(multibranchJob.getJob(FAILED_JOB), false);

        // When add new branch dynamically via git push
        String newBranch = "feature-dynamic";
        try (GitRepo repo = new GitRepo(container.repoUrl(USERNAME + "/" + REPO_NAME, userToken))) {
            repo.git("checkout", "-b", newBranch);
            repo.touch("marker-" + System.currentTimeMillis() + ".txt");
            repo.git("add", ".");
            repo.git("commit", "-m", "Add marker file to feature branch");
            repo.git("push", "origin", newBranch);
        }

        try (var gitlabapi = new GitLabApi(container.getHttpUrl(), userToken)
                .withRequestTimeout(GITLAB_API_CONNECT_TIMEOUT_MS, GITLAB_API_READ_TIMEOUT_MS)) {
            awaitBranchAvailabilityViaApi(gitlabapi, project.getId(), newBranch);
        }

        // Then the branch is discovered
        final WorkflowJob newBranchJob = awaitBranchDiscoveryViaJenkins(multibranchJob, newBranch);
        assertExistAndResult(newBranchJob, true);

        // When add tags to repository
        try (GitRepo repo = new GitRepo(container.repoUrl(USERNAME + "/" + REPO_NAME, userToken))) {
            repo.git("checkout", MAIN_BRANCH);
            repo.git("tag", "-a", "v1.0.0", "-m", "Release v1.0.0");
            repo.git("push", "origin", "v1.0.0");
            repo.git("tag", "-a", "v2.0.0", "-m", "Release v2.0.0");
            repo.git("push", "origin", "v2.0.0");
        }

        try (var gitlabapi = new GitLabApi(container.getHttpUrl(), userToken)
                .withRequestTimeout(GITLAB_API_CONNECT_TIMEOUT_MS, GITLAB_API_READ_TIMEOUT_MS)) {
            String projectPath = container.extractProjectPath(container.repoUrl(USERNAME + "/" + REPO_NAME, userToken));
            awaitTagsAvailabilityViaApi(gitlabapi, projectPath, "v1.0.0", "v2.0.0");
        }

        // When multibranch job with tag discovery scans repository
        final WorkflowMultiBranchJob tagMultibranchJob = jenkins.jobs.create(WorkflowMultiBranchJob.class);
        var tagBranchSource = configureJobWithGitLabBranchSource(tagMultibranchJob, USERNAME, REPO_NAME);
        tagBranchSource.enableTagDiscovery();
        tagMultibranchJob.save();

        tagMultibranchJob.waitForBranchIndexingFinished((int) BRANCH_INDEXING_TIMEOUT.toSeconds());

        // Then tags discovered and jobs created (builds not auto-triggered)
        String tagIndexingLog = tagMultibranchJob.getBranchIndexingLogText();
        assertThat(tagIndexingLog, containsString("Finished: SUCCESS"));
        assertThat(tagIndexingLog, containsString("v1.0.0"));
        assertThat(tagIndexingLog, containsString("v2.0.0"));

        assertJobExists(tagMultibranchJob.getJob("v1.0.0"));
        assertJobExists(tagMultibranchJob.getJob("v2.0.0"));
    }

    /**
     * Verifies organization folder discovers group projects and indexes their branches/MRs.
     */
    @Test
    public void gitLabGroupFolderOrganization() throws GitLabApiException, IOException {
        // Given a GitLab group with 2 projects, each with 4 branches and 1 merge request
        try (var gitlabapi = new GitLabApi(container.getHttpUrl(), adminToken)
                .withRequestTimeout(GITLAB_API_CONNECT_TIMEOUT_MS, GITLAB_API_READ_TIMEOUT_MS)) {
            var group = createGroupViaApi(gitlabapi.getGroupApi(), GROUP_NAME, GROUP_NAME);

            var userId = gitlabapi.getUserApi().getOptionalUser(USERNAME).get().getId();
            addGroupMemberViaApi(gitlabapi.getGroupApi(), group.getId(), userId, AccessLevel.DEVELOPER);

            var project1 = createProjectViaApi(
                    gitlabapi.getProjectApi(),
                    new Project().withPublic(false).withPath(REPO_NAME).withNamespaceId(group.getId()));

            String repoUrl = container.repoUrl(GROUP_NAME + "/" + REPO_NAME, adminToken);
            setUpInitialBranchViaGit(repoUrl, MAIN_BRANCH, JENKINSFILE_CONTENT);
            setupBranchFromViaGit(repoUrl, FIRST_BRANCH, MAIN_BRANCH, JENKINSFILE_CONTENT);
            setupBranchFromViaGit(repoUrl, SECOND_BRANCH, MAIN_BRANCH, JENKINSFILE_CONTENT);
            setupBranchFromViaGit(repoUrl, FAILED_JOB, MAIN_BRANCH, BROKEN_JENKINSFILE);
            MergeRequest mr1 = createMergeRequestViaApi(gitlabapi, project1, SECOND_BRANCH, MAIN_BRANCH, "test_mr");
            awaitMergeRequestMergeRefViaApi(gitlabapi, project1.getId(), mr1.getIid());

            var project2 = createProjectViaApi(
                    gitlabapi.getProjectApi(),
                    new Project().withPublic(false).withPath(ANOTHER_REPO_NAME).withNamespaceId(group.getId()));

            String anotherRepoUrl = container.repoUrl(GROUP_NAME + "/" + ANOTHER_REPO_NAME, adminToken);
            setUpInitialBranchViaGit(anotherRepoUrl, MAIN_BRANCH, JENKINSFILE_CONTENT);
            setupBranchFromViaGit(anotherRepoUrl, FIRST_BRANCH, MAIN_BRANCH, JENKINSFILE_CONTENT);
            setupBranchFromViaGit(anotherRepoUrl, SECOND_BRANCH, MAIN_BRANCH, JENKINSFILE_CONTENT);
            setupBranchFromViaGit(anotherRepoUrl, FAILED_JOB, MAIN_BRANCH, BROKEN_JENKINSFILE);
            MergeRequest mr2 = createMergeRequestViaApi(gitlabapi, project2, SECOND_BRANCH, MAIN_BRANCH, "test_mr");
            awaitMergeRequestMergeRefViaApi(gitlabapi, project2.getId(), mr2.getIid());
        }

        createGitLabToken(adminToken, "GitLab Personal Access Token");
        configureGitLabServer();

        // When organization folder scans GitLab group
        final GitLabOrganizationFolder organizationFolder = jenkins.jobs.create(GitLabOrganizationFolder.class);
        organizationFolder.create(GROUP_NAME);
        organizationFolder.save();

        organizationFolder.waitForCheckFinished(BRANCH_INDEXING_TIMEOUT);

        // Then both projects discovered, all branches indexed and built
        assertThat(organizationFolder.getCheckLog(), containsString("Finished: SUCCESS"));

        organizationFolder.open();
        waitFor().withTimeout(PAGE_LOAD_TIMEOUT).until(() -> driver.getPageSource()
                .contains("Scan GitLab Group Now"));

        var projects = List.of(
                organizationFolder.getJobs().get(WorkflowMultiBranchJob.class, GROUP_NAME + "%2F" + REPO_NAME),
                organizationFolder.getJobs().get(WorkflowMultiBranchJob.class, GROUP_NAME + "%2F" + ANOTHER_REPO_NAME));

        for (var project : projects) {
            assertExistAndResult(project.getJob(MAIN_BRANCH), true);
            assertExistAndResult(project.getJob(FIRST_BRANCH), true);
            assertExistAndResult(project.getJob("MR-1"), true);
            assertExistAndResult(project.getJob(FAILED_JOB), false);
        }
    }

    // ==================== Helper Methods ====================

    /**
     * GitLab uses "Scan GitLab Project Now" instead of generic "Scan Repository Now"
     */
    private void reIndex(WorkflowMultiBranchJob job) {
        job.open();
        // click the parent <a> tag
        final List<WebElement> scanGitLabNow = driver.findElements(
                by.xpath("//a[span[@class='task-link-text' and text()='Scan GitLab Project Now']]"));

        if (!scanGitLabNow.isEmpty()) {
            scanGitLabNow.get(0).click();
            return;
        }

        // Fallback to generic implementation
        job.reIndex();
    }

    private void createGitLabToken(String token, String id) {
        var cp = new CredentialsPage(jenkins, ManagedCredentials.DEFAULT_DOMAIN);
        cp.open();

        var tk = cp.add(GitLabPersonalAccessTokenCredential.class);
        tk.setToken(token);
        tk.setId(id);
        tk.create();
    }

    private void configureGitLabServer() throws IOException {
        jenkins.configure();
        new GitLabServerConfig(jenkins).configureServer(container.getHttpUrl());
        jenkins.save();
    }

    private GitLabBranchSource configureJobWithGitLabBranchSource(
            final WorkflowMultiBranchJob job, String owner, String project) {
        var branchSource = job.addBranchSource(GitLabBranchSource.class);
        branchSource.setOwner(owner);
        branchSource.find(branchSource.by.path("/sources/source/projectPath")).click();

        String fullPath = owner + "/" + project;
        branchSource
                .waitFor()
                .withMessage("Waiting for GitLab project '%s' to appear in dropdown", fullPath)
                .withTimeout(GITLAB_API_RETRY_TIMEOUT)
                .ignoring(NoSuchElementException.class)
                .until(() -> branchSource.find(branchSource.by.option(fullPath)) != null);
        branchSource.waitFor(branchSource.by.option(fullPath)).click();
        return branchSource;
    }

    private void assertExistAndResult(final WorkflowJob job, final boolean withSuccess) {
        final Build.Result expectedResult = withSuccess ? Build.Result.SUCCESS : Build.Result.FAILURE;
        assertJobExists(job);
        waitFor()
                .withMessage("Waiting for job '%s' to complete with result %s", job, expectedResult)
                .withTimeout(BUILD_COMPLETION_TIMEOUT)
                .pollingEvery(POLLING_INTERVAL)
                .ignoring(NoSuchElementException.class)
                .until(() -> {
                    Build lastBuild = job.getLastBuild();
                    if (lastBuild == null) {
                        return false;
                    }
                    String result = lastBuild.getResult();
                    return result != null && result.contains(expectedResult.name());
                });
    }

    private void assertJobExists(final WorkflowJob job) {
        waitFor()
                .withMessage("Waiting for job '%s' to be created", job)
                .withTimeout(JOB_CREATION_TIMEOUT)
                .pollingEvery(POLLING_INTERVAL)
                .ignoring(UncheckedIOException.class)
                .until(() -> {
                    checkCanOpenJobUrl(job);
                    return true;
                });
    }

    private static void checkCanOpenJobUrl(final WorkflowJob job) throws UncheckedIOException {
        try (var unused = job.url("").openStream()) {
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    // ==================== Git operations ====================

    private void setUpInitialBranchViaGit(String repoUrl, String branchName, String content) throws IOException {
        try (GitRepo repo = new GitRepo(repoUrl)) {
            repo.changeAndCommitFile("Jenkinsfile", content, "Initial commit");
            repo.git("push", "-u", "origin", branchName);
        }
    }

    private void setupBranchFromViaGit(String repoUrl, String branchName, String sourceRef, String content)
            throws IOException {
        try (GitRepo repo = new GitRepo(repoUrl)) {
            repo.git("fetch", "origin");
            repo.git("checkout", "-b", branchName, "origin/" + sourceRef);
            repo.changeAndCommitFile("Jenkinsfile", content, "Add/update Jenkinsfile");
            repo.git("push", "origin", branchName);
        }
    }

    // ==================== GitLab API ====================

    private Project createProjectViaApi(ProjectApi projApi, Project projectSpec) {
        return waitFor()
                .withMessage("Creating GitLab project '%s'", projectSpec.getPath())
                .withTimeout(GITLAB_API_RETRY_TIMEOUT)
                .until(() -> {
                    try {
                        return projApi.createProject(projectSpec);
                    } catch (Exception e) {
                        try {
                            return projApi.getProject(projectSpec.getPath());
                        } catch (Exception ex) {
                            return null;
                        }
                    }
                });
    }

    private Group createGroupViaApi(GroupApi groupApi, String groupName, String groupPath) {
        var groupParams =
                new GroupParams().withName(groupName).withPath(groupPath).withMembershipLock(false);
        return waitFor()
                .withMessage("Creating GitLab group '%s'", groupName)
                .withTimeout(GITLAB_API_RETRY_TIMEOUT)
                .until(() -> {
                    try {
                        return groupApi.createGroup(groupParams).withVisibility(Visibility.PRIVATE);
                    } catch (Exception e) {
                        try {
                            return groupApi.getGroup(groupPath);
                        } catch (Exception ex) {
                            return null;
                        }
                    }
                });
    }

    private Member addGroupMemberViaApi(GroupApi groupApi, Long groupId, Long userId, AccessLevel accessLevel) {
        return waitFor()
                .withMessage("Adding user %d to group %d", userId, groupId)
                .withTimeout(GITLAB_API_RETRY_TIMEOUT)
                .until(() -> {
                    try {
                        return groupApi.addMember(groupId, userId, accessLevel);
                    } catch (Exception e) {
                        try {
                            return groupApi.getMember(groupId, userId);
                        } catch (Exception ex) {
                            return null;
                        }
                    }
                });
    }

    private MergeRequest createMergeRequestViaApi(
            GitLabApi gitlabapi, Project project, String sourceBranch, String targetBranch, String mrTitle) {
        var params = new MergeRequestParams()
                .withSourceBranch(sourceBranch)
                .withTargetBranch(targetBranch)
                .withTitle(mrTitle);
        return waitFor()
                .withMessage("Creating merge request '%s'", mrTitle)
                .withTimeout(GITLAB_API_RETRY_TIMEOUT)
                .until(() -> {
                    try {
                        return gitlabapi.getMergeRequestApi().createMergeRequest(project, params);
                    } catch (Exception e) {
                        try {
                            var filter = new MergeRequestFilter().withProjectId(project.getId());
                            return gitlabapi.getMergeRequestApi().getMergeRequests(filter).stream()
                                    .filter(mr -> mrTitle.equals(mr.getTitle()))
                                    .findFirst()
                                    .orElse(null);
                        } catch (Exception ex) {
                            return null;
                        }
                    }
                });
    }

    private void awaitBranchAvailabilityViaApi(GitLabApi gitlabapi, Long projectId, String branchName) {
        Instant startTime = Instant.now();
        waitFor()
                .withMessage("Waiting for GitLab branch '%s' to be available", branchName)
                .withTimeout(GITLAB_API_RETRY_TIMEOUT)
                .ignoring(GitLabApiRuntimeException.class)
                .until(() -> {
                    try {
                        var branch = gitlabapi.getRepositoryApi().getBranch(projectId, branchName);
                        return branch != null && branch.getCommit() != null;
                    } catch (GitLabApiException e) {
                        throw new GitLabApiRuntimeException(e);
                    }
                });
        System.out.println("GitLab branch '" + branchName + "' wait: " + elapsed(startTime));
    }

    private WorkflowJob awaitBranchDiscoveryViaJenkins(WorkflowMultiBranchJob multibranchJob, String branchName) {
        Instant startTime = Instant.now();
        WorkflowJob job = waitFor()
                .withMessage("Waiting for Jenkins to discover branch '%s'", branchName)
                .withTimeout(BRANCH_INDEXING_TIMEOUT)
                .pollingEvery(POLLING_INTERVAL)
                .ignoring(UncheckedIOException.class, NoSuchElementException.class)
                .until(() -> {
                    try {
                        WorkflowJob existingJob = multibranchJob.getJob(branchName);
                        checkCanOpenJobUrl(existingJob);
                        return existingJob;
                    } catch (UncheckedIOException e) {
                        System.err.println(branchName + " not found, expected, attempting reIndex ...");
                    }

                    reIndex(multibranchJob);
                    multibranchJob.waitForBranchIndexingFinished((int) BRANCH_INDEXING_TIMEOUT.toSeconds());
                    String indexingLog = multibranchJob.getBranchIndexingLogText();

                    if (!indexingLog.contains("Finished: SUCCESS")
                            || !indexingLog.contains("Scheduled build for branch: " + branchName)) {
                        throw new NoSuchElementException(
                                "reIndex unsuccessful or branch " + branchName + " could not be found: " + indexingLog);
                    }

                    WorkflowJob discoveredJob = multibranchJob.getJob(branchName);
                    checkCanOpenJobUrl(discoveredJob);
                    return discoveredJob;
                });
        System.out.println("Jenkins branch '" + branchName + "' discovery: " + elapsed(startTime));
        return job;
    }

    private void awaitTagsAvailabilityViaApi(GitLabApi gitlabapi, String projectPath, String... tagNames) {
        Instant startTime = Instant.now();
        for (String tagName : tagNames) {
            waitFor()
                    .withMessage("Waiting for tag '%s' in GitLab API", tagName)
                    .withTimeout(GITLAB_API_RETRY_TIMEOUT)
                    .ignoring(GitLabApiRuntimeException.class, AssertionError.class)
                    .until(() -> {
                        try {
                            var tags = gitlabapi.getTagsApi().getTags(projectPath);
                            assertThat(tags, is(notNullValue()));
                            assertThat(tags.stream().map(Tag::getName).toList(), hasItems(tagNames));
                            return true;
                        } catch (GitLabApiException e) {
                            throw new GitLabApiRuntimeException(e);
                        }
                    });
        }
        System.out.println("GitLab tags wait: " + elapsed(startTime));
    }

    private void awaitMergeRequestMergeRefViaApi(GitLabApi gitlabapi, Long projectId, Long mrIid) {
        Instant startTime = Instant.now();
        String mergeRef = "refs/merge-requests/" + mrIid + "/merge";
        waitFor()
                .withMessage("Waiting for GitLab MR %d merge ref to be available", mrIid)
                .withTimeout(GITLAB_API_RETRY_TIMEOUT)
                .ignoring(GitLabApiRuntimeException.class)
                .until(() -> {
                    try {
                        return gitlabapi.getRepositoryFileApi().getFile(projectId, "Jenkinsfile", mergeRef);
                    } catch (GitLabApiException e) {
                        throw new GitLabApiRuntimeException(e);
                    }
                });
        System.out.println("GitLab MR " + mrIid + " merge ref wait: " + elapsed(startTime));
    }

    // To use in {@code .ignoring}
    private static class GitLabApiRuntimeException extends RuntimeException {
        GitLabApiRuntimeException(GitLabApiException e) {
            super(e);
        }
    }

    private static Duration elapsed(Instant startTime) {
        return Duration.between(startTime, Instant.now());
    }
}
