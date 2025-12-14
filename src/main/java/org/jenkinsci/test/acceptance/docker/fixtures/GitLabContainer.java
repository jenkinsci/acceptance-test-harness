package org.jenkinsci.test.acceptance.docker.fixtures;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.jenkinsci.test.acceptance.docker.Docker;
import org.jenkinsci.test.acceptance.docker.DockerContainer;
import org.jenkinsci.test.acceptance.docker.DockerFixture;
import org.jenkinsci.test.acceptance.po.CapybaraPortingLayer;
import org.jenkinsci.test.acceptance.utils.ElasticTime;

@DockerFixture(
        id = "gitlab-plugin",
        ports = {80, 443, 22})
public class GitLabContainer extends DockerContainer {
    public static final int GITLAB_API_CONNECT_TIMEOUT_MS = 30_000;
    public static final int GITLAB_API_READ_TIMEOUT_MS = 120_000;

    private static final Duration READINESS_TIMEOUT = Duration.ofMinutes(10);
    private static final Duration READINESS_POLL_INTERVAL = Duration.ofSeconds(10);
    private static final Duration READINESS_REQUEST_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration READINESS_CONNECTION_TIMEOUT = Duration.ofMillis(500);

    private static final ElasticTime time = new ElasticTime();

    private static Duration time(Duration duration) {
        return Duration.ofMillis(time.milliseconds(duration.toMillis()));
    }

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

    public String getHttpUrl() {
        return "http://" + httpHost() + ":" + httpPort();
    }

    /**
     * @return Authenticated Git URL ("http://username:token@host:port/username/repo.git")
     */
    public String repoUrl(String projectPath, String token) {
        String username = projectPath.split("/")[0];
        return getHttpUrl().replace("://", "://" + username + ":" + token + "@") + "/" + projectPath + ".git";
    }

    /**
     * Extracts the GitLab project path from an authenticated Git repository URL.
     *
     * @param repoUrl see {@link GitLabContainer#repoUrl(String, String)}
     * @return Project path ("username/repo")
     */
    public String extractProjectPath(String repoUrl) {
        String afterAuth = repoUrl.split("@")[1];
        return afterAuth.split("/", 2)[1].replace(".git", "");
    }

    public void waitForReady(CapybaraPortingLayer p) {
        var client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(time(READINESS_CONNECTION_TIMEOUT))
                .build();

        p.waitFor()
                .withMessage("Waiting for GitLab to come up")
                .withTimeout(READINESS_TIMEOUT)
                .pollingEvery(READINESS_POLL_INTERVAL)
                .ignoring(UncheckedIOException.class)
                .until(() -> {
                    try {
                        var request = HttpRequest.newBuilder()
                                .uri(new URL(getHttpUrl()).toURI())
                                .GET()
                                .timeout(time(READINESS_REQUEST_TIMEOUT))
                                .build();
                        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
                        return response.body().contains("GitLab Community Edition");
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
    }

    public String createUserToken(String userName, String password, String email, String isAdmin)
            throws IOException, InterruptedException {
        return Docker.cmd("exec", getCid())
                .add(
                        "/bin/bash",
                        "-c",
                        "gitlab-rails runner -e production /usr/bin/create_user.rb" + " " + userName + " " + password
                                + " " + email + " " + isAdmin)
                .popen()
                .verifyOrDieWith("Unable to create user")
                .trim();
    }

    // attempt to cleanup, no retry
    public void cleanup(String token, String repoName, String groupName) {
        try (var gitlabapi = new GitLabApi(getHttpUrl(), token)
                .withRequestTimeout(GITLAB_API_CONNECT_TIMEOUT_MS, GITLAB_API_READ_TIMEOUT_MS)) {
            try {
                gitlabapi.getProjectApi().getProjects(repoName).stream()
                        .filter(proj -> repoName.equals(proj.getName()))
                        .findFirst()
                        .ifPresent(project -> {
                            try {
                                gitlabapi.getProjectApi().deleteProject(project);
                            } catch (GitLabApiException e) {
                                System.err.println("Failed to delete project '" + repoName + "': " + e.getMessage());
                            }
                        });
            } catch (Exception e) {
                System.err.println("Failed to list projects for '" + repoName + "': " + e.getMessage());
            }

            try {
                gitlabapi.getGroupApi().getGroups(groupName).stream()
                        .filter(g -> groupName.equals(g.getName()))
                        .findFirst()
                        .ifPresent(group -> {
                            try {
                                gitlabapi.getGroupApi().deleteGroup(group.getId());
                            } catch (GitLabApiException e) {
                                System.err.println("Failed to delete group '" + groupName + "': " + e.getMessage());
                            }
                        });
            } catch (Exception e) {
                System.err.println("Failed to list groups for '" + groupName + "': " + e.getMessage());
            }
        }
    }
}
