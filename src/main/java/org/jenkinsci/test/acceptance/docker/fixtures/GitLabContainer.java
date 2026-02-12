package org.jenkinsci.test.acceptance.docker.fixtures;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import org.jenkinsci.test.acceptance.docker.Docker;
import org.jenkinsci.test.acceptance.docker.DockerContainer;
import org.jenkinsci.test.acceptance.docker.DockerFixture;
import org.jenkinsci.test.acceptance.po.CapybaraPortingLayerImpl;
import org.openqa.selenium.WebDriverException;

@DockerFixture(
        id = "gitlab-plugin",
        ports = {80, 443, 22})
public class GitLabContainer extends DockerContainer {
    private static final Duration READINESS_TIMEOUT = Duration.ofMinutes(5);
    private static final Duration READINESS_POLL_INTERVAL = Duration.ofSeconds(5);

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

    public void waitForReady(CapybaraPortingLayerImpl p) throws MalformedURLException {
        GitLabPage gitLabPage = new GitLabPage(p.injector, new URL(getHttpUrl()));
        p.waitFor()
                .withMessage("Waiting for GitLab to come up")
                .withTimeout(READINESS_TIMEOUT)
                .pollingEvery(READINESS_POLL_INTERVAL)
                .ignoring(WebDriverException.class) // connection reset while not up
                .until(() -> {
                    gitLabPage.open();
                    return gitLabPage.isReady();
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
}
