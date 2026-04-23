package org.jenkinsci.test.acceptance.docker.fixtures;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import org.jenkinsci.test.acceptance.po.CapybaraPortingLayerImpl;
import org.openqa.selenium.WebDriverException;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.images.builder.ImageFromDockerfile;

public class GitLabContainer extends GenericContainer<GitLabContainer> {
    private static final Duration READINESS_TIMEOUT = Duration.ofMinutes(5);
    private static final Duration READINESS_POLL_INTERVAL = Duration.ofSeconds(5);

    /**
     * Create a GitLabContainer.
     * <strong>starting this container takes a long time</strong>, as such waiting for it to be ready
     * must use WebDriver via #waitForReady
     */
    public GitLabContainer() {
        super(new ImageFromDockerfile("localhost/testcontainers/ath-gitlab", false)
                .withFileFromClasspath(".", GitLabContainer.class.getName().replace('.', '/')));
        withExposedPorts(80, 443, 22);
        withSharedMemorySize(1073741824L);
        withCreateContainerCmdModifier(
                cmd -> cmd.getHostConfig().withMemory(4L * 1024 * 1024 * 1024).withMemorySwap(5L * 1024 * 1024 * 1024));
        // Startup takes too long which causes the selenium session to timeout so consumers must call waitForReady
        // which polls using webdriver
        /*
        waitingFor(new HttpWaitStrategy()
                .forPort(80)
                .forStatusCode(200)
                .forResponsePredicate(response -> response.contains("GitLab Community Edition"))
                .withStartupTimeout(READINESS_TIMEOUT));
        */
    }

    public String host() {
        return getHost();
    }

    public int sshPort() {
        return getMappedPort(22);
    }

    public int httpPort() {
        return getMappedPort(80);
    }

    public String httpHost() {
        return getHost();
    }

    public String getHttpUrl() {
        return "http://" + getHost() + ":" + getMappedPort(80);
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
                .ignoring(WebDriverException.class)
                .until(() -> {
                    gitLabPage.open();
                    return gitLabPage.isReady();
                });
    }

    public String createUserToken(String userName, String password, String email, String isAdmin)
            throws IOException, InterruptedException {
        var result = execInContainer(
                "/bin/bash",
                "-c",
                "gitlab-rails runner -e production /usr/bin/create_user.rb" + " " + userName + " " + password + " "
                        + email + " " + isAdmin);
        if (result.getExitCode() != 0) {
            throw new IOException("Unable to create user: " + result.getStderr());
        }
        return result.getStdout().trim();
    }
}
