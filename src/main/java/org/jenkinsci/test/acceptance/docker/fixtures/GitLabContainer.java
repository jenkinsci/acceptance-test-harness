package org.jenkinsci.test.acceptance.docker.fixtures;

import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.GroupApi;
import org.gitlab4j.api.ProjectApi;
import org.gitlab4j.api.models.*;
import org.jenkinsci.test.acceptance.docker.Docker;
import org.jenkinsci.test.acceptance.docker.DockerContainer;
import org.jenkinsci.test.acceptance.docker.DockerFixture;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.jenkinsci.test.acceptance.po.CapybaraPortingLayer;
import org.jenkinsci.test.acceptance.utils.ElasticTime;


@DockerFixture(id = "gitlab-plugin", ports = {80, 443, 22})
public class GitLabContainer extends DockerContainer {
    protected static final String REPO_DIR = "/home/gitlab/gitlabRepo";

    private static final HttpClient client = HttpClient.newBuilder()
                                                       .followRedirects(HttpClient.Redirect.NORMAL)
                                                        .connectTimeout(Duration.ofMillis(200))
                                                       .build();

    private static final ElasticTime time = new ElasticTime();

    public String host() {
        return ipBound(22);
    }

    public int port() {
        return port(22);
    }


    public int webPort() {
        return port(443);
    }

    public String webHost() {
        return ipBound(443);
    }

    public URL getURL() throws IOException {
        return new URL("http://" + getIpAddress() + port());
    }

    public URL getHttpUrl() throws IOException {
        String url = "http://" + ipBound(80) + ':' + port(80);
        return new URL(url);
    }

    /** URL visible from the host. */
    public String getRepoUrl() {
        return "ssh://git@" + host() + ":" + port() + REPO_DIR;
    }

    @Deprecated
    public String getRepoUrlInsideDocker() throws IOException {
        return "ssh://git@" + getIpAddress() + REPO_DIR;
    }

    /**
     * URL visible from other Docker containers.
     * @param alias an alias for this container’s {@link #getCid} passed to {@code --link}
     */
    public String getRepoUrlInsideDocker(String alias) throws IOException {
        return "ssh://git@" + alias + REPO_DIR;
    }

    public void waitForReady(CapybaraPortingLayer p) {
        long timeout =  time.seconds(200); // GitLab starts in about 2 minutes add some headway
        p.waitFor().withMessage("Waiting for GitLab to come up")
                .withTimeout(Duration.ofMillis(timeout))
                .pollingEvery(Duration.ofSeconds(2))
                .until( () ->  {
                    try {
                        HttpRequest request = HttpRequest.newBuilder()
                                .uri(getHttpUrl().toURI())
                                .GET()
                                .timeout(Duration.ofSeconds(1))
                                .build();
                        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                        return response.body().contains("GitLab Community Edition");
                    } catch (IOException ignored) {
                        // we can not use .ignoring as this is a checked exception (even though a callable can throw this!)
                        return Boolean.FALSE;
                    }

                });
    }

    public HttpResponse<String> createRepo(String repoName, String token) throws IOException {
        try{
            HttpRequest request = HttpRequest.newBuilder()
                                             .uri(new URI(getHttpUrl() + "/api/v4/projects"))
                                             .header("Content-Type", "application/json")
                                             .header("PRIVATE-TOKEN", token)
                                             .POST(HttpRequest.BodyPublishers.ofString("{ \"name\": \"" + repoName + "\" }"))
                                             .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void createBranch(String token, String repoName) throws IOException, GitLabApiException {
        GitLabApi gitlabapi = new GitLabApi("http://" + getIpAddress(), token);
        ProjectApi projApi = new ProjectApi(gitlabapi);
        Project project = projApi.getProjects().stream().filter((proj -> repoName.equals(proj.getName()))).findAny().orElse(null);

        RepositoryFile file = new RepositoryFile();
        file.setFileName("Jenkinsfile");
        file.setFilePath("Jenkinsfile");
        file.setContent("pipeline {\n" +
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
                "}");

        // create Jenkinsfile on the main branch
        gitlabapi.getRepositoryFileApi().createFile(project.getId(), file, "main", "Add Jenkinsfile");

        // create 2 new branches
        gitlabapi.getRepositoryApi().createBranch(project.getId(), "firstbranch", "main");
        gitlabapi.getRepositoryApi().createBranch(project.getId(), "secondbranch", "main");

        // add a file on the secondbranch
        RepositoryFile newFile = new RepositoryFile();
        newFile.setFileName("README.md");
        newFile.setFilePath("readme.md");
        newFile.setContent("read me");
        gitlabapi.getRepositoryFileApi().createFile(project.getId(), newFile, "secondbranch", "Add readme");


        // create a branch with a broken Jenkinsfile
        gitlabapi.getRepositoryApi().createBranch(project.getId(), "failedjob", "main");
        file.setContent("pipeline {\n" +
                "    agent any\n" );
        gitlabapi.getRepositoryFileApi().updateFile(project.getId(), file, "failedjob", "this will not work");

        // create a MR
        MergeRequestParams params = new MergeRequestParams()
                .withSourceBranch("secondbranch")
                .withTargetBranch("main")
                .withTitle("test_mr");
        gitlabapi.getMergeRequestApi().createMergeRequest(project, params);
    }

    public void deleteRepo(String token, String repoName) throws IOException, GitLabApiException {
        // get the project and delete the project
        GitLabApi gitlabapi = new GitLabApi(getHttpUrl().toString(), token);
        ProjectApi projApi = new ProjectApi(gitlabapi);

        Project project = projApi.getProjects().stream().filter((proj -> repoName.equals(proj.getName()))).findAny().orElse(null);
        projApi.deleteProject(project);
    }

    public String createUserToken(String userName, String password, String email, String isAdmin) throws IOException, InterruptedException {
        return Docker.cmd("exec", getCid()).add("/bin/bash",  "-c", "gitlab-rails runner -e production /usr/bin/create_user.rb" + " " + userName + " " + password + " " + email + " " + isAdmin)
                .popen()
                .verifyOrDieWith("Unable to create user").trim();
    }

    public void createGroup(String groupName, String userName, String privateTokenAdmin, String repoName, String anotherRepoName) throws IOException, GitLabApiException {
        GitLabApi gitlabapi = new GitLabApi(getHttpUrl().toString(), privateTokenAdmin);
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

        // populate the repository
        createBranch(privateTokenAdmin, repoName);

        // create another project within the group
        project = new Project().withPublic(false)
                .withPath(anotherRepoName)
                .withNamespaceId(group.getId());

        projApi.createProject(project);

        // populate the repository
        createBranch(privateTokenAdmin, anotherRepoName);
    }
}