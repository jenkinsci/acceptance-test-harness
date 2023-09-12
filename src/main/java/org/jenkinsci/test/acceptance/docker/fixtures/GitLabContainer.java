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

    public int sshPort() {
        return port(22);
    }


    public int httpPort() {
        return port(80);
    }

    public String httpHost() {
        return ipBound(80);
    }

    public URL getURL() throws IOException {
        return new URL("http://" + getIpAddress() + sshPort());
    }

    public URL getHttpUrl() throws IOException {
        String url = "http://" + httpHost() + ':' + httpPort();
        return new URL(url);
    }

    /** URL visible from the host. */
    public String getRepoUrl() {
        return "ssh://git@" + host() + ":" + sshPort() + REPO_DIR;
    }

    public void waitForReady(CapybaraPortingLayer p) {
        p.waitFor().withMessage("Waiting for GitLab to come up")
                .withTimeout(Duration.ofSeconds(200)) // GitLab starts in about 2 minutes add some headway
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

    public HttpResponse<String> createRepo(String repoName, String token) throws RuntimeException {
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
}