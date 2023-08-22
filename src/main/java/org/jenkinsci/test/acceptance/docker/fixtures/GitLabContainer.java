package org.jenkinsci.test.acceptance.docker.fixtures;

import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.ProjectApi;
import org.gitlab4j.api.models.Project;
import org.jenkinsci.test.acceptance.docker.Docker;
import org.jenkinsci.test.acceptance.docker.DockerContainer;
import org.jenkinsci.test.acceptance.docker.DockerFixture;

import java.io.IOException;
import java.net.SocketException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.jenkinsci.test.acceptance.po.CapybaraPortingLayer;

import static org.junit.Assert.assertTrue;


@DockerFixture(id = "gitlab-plugin", ports = 22)
public class GitLabContainer extends DockerContainer {
    protected static final String REPO_DIR = "/home/gitlab/gitlabRepo";

    private static final HttpClient client = HttpClient.newBuilder()
                                                       .followRedirects(HttpClient.Redirect.NORMAL)
                                                       .connectTimeout(Duration.ofSeconds(5))
                                                       .build();

    public String host() {
        return ipBound(22);
    }

    public int port() {
        return port(22);
    }

    public URL getURL() throws IOException {
        // return new URL("http://" + host() + ":" + port());
        return new URL("http://" + getIpAddress());
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

    public HttpResponse<String> createRepo(String repoName, String token) throws IOException {
        try{
            HttpRequest request = HttpRequest.newBuilder()
                                             .uri(new URI("http://" + getIpAddress() + "/api/v4/projects"))
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

    public void deleteRepo(String token, String repoName) throws IOException, GitLabApiException {
        // get the project and delete the project
        GitLabApi gitlabapi = new GitLabApi("http://" + getIpAddress(), token);
        ProjectApi projApi = new ProjectApi(gitlabapi);

        Project project = projApi.getProjects().stream().filter((proj -> repoName.equals(proj.getName()))).findAny().orElse(null);
        projApi.deleteProject(project);
    }

    public void waitForReady(CapybaraPortingLayer p) {
        p.waitFor().withMessage("Waiting for GitLab to come up")
                .withTimeout(Duration.ofSeconds(200)) // GitLab starts in about 2 minutes
                .until( () ->  {
                    try {
                          HttpRequest request = HttpRequest.newBuilder()
                                .uri(new URI("http://" + getIpAddress()))
                                .GET()
                                .build();
                        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                        return response.body().contains("GitLab Community Edition");
                    } catch (SocketException e) {
                        return null;
                    }

                });
    }

    public String createUserToken() throws IOException, InterruptedException {
        return Docker.cmd("exec", getCid()).add("/bin/bash",  "-c", "gitlab-rails runner -e production /usr/bin/create_user.rb")
                .popen()
                .verifyOrDieWith("Unable to create user").trim();
    }
}