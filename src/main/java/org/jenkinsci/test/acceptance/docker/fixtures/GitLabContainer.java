package org.jenkinsci.test.acceptance.docker.fixtures;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.Map;
import org.jenkinsci.test.acceptance.po.CapybaraPortingLayerImpl;
import org.openqa.selenium.WebDriverException;
import org.testcontainers.containers.ContainerLaunchException;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.InternetProtocol;
import org.testcontainers.containers.wait.strategy.WaitStrategy;
import org.testcontainers.containers.wait.strategy.WaitStrategyTarget;
import org.testcontainers.images.builder.ImageFromDockerfile;

public class GitLabContainer extends GenericContainer<GitLabContainer> {
    private static final Duration READINESS_TIMEOUT = Duration.ofMinutes(3);
    private static final Duration READINESS_POLL_INTERVAL = Duration.ofSeconds(5);
    // non-privaledged non-ephemeral ports that are not likely to be in use.
    private static final int FIXED_SSH_PORT = 6622;
    private static final int FIXED_HTTP_PORT = 6680;

    // we need to tell the container what its hostname is
    private final String hostname;

    /**
     * Create a GitLabContainer.
     * <strong>starting this container takes a long time</strong>, as such callers must use {@link #start(CapybaraPortingLayerImpl)} rather than {@link #start()}.
     */
    @SuppressWarnings("resource")
    public GitLabContainer() {
        super(new ImageFromDockerfile("localhost/testcontainers/ath-gitlab", false)
                .withFileFromClasspath(".", GitLabContainer.class.getName().replace('.', '/')));
        // GitLab API puts the full URL for repos in the API responses.
        // so things like branchsource can call the API but then it will fail as it uses the container address and not
        // the mapped ports. It also uses a hostname (obtained dynamically which will be the container hostname)
        // all of this is a big chicken and egg problem so we fix the ports (using no privileged ports to avoid extra
        // risk of collision)
        addFixedExposedPort(FIXED_HTTP_PORT, FIXED_HTTP_PORT, InternetProtocol.TCP);
        // ssh still binds in the container to port 22.
        addFixedExposedPort(FIXED_SSH_PORT, 22, InternetProtocol.TCP);
        // and we fix the hostname
        hostname = DockerClientFactory.instance().dockerHostIpAddress();
        withEnv(Map.of(
                "GITLAB_OMNIBUS_CONFIG",
                "external_url 'http://" + hostname + ":" + FIXED_HTTP_PORT
                        + "'; gitlab_rails['gitlab_shell_ssh_port'] = " + FIXED_SSH_PORT));
        withSharedMemorySize(1L * 1024 * 1024 * 1024);

        withCreateContainerCmdModifier(
                cmd -> cmd.getHostConfig().withMemory(4L * 1024 * 1024 * 1024).withMemorySwap(5L * 1024 * 1024 * 1024));
    }

    @Override
    public String getHost() {
        return hostname;
        // return super.getHost();
    }

    public String host() {
        return getHost();
    }

    public int sshPort() {
        return FIXED_SSH_PORT;
    }

    public int httpPort() {
        return FIXED_HTTP_PORT;
    }

    public String httpHost() {
        return getHost();
    }

    public String getHttpUrl() {
        return "http://" + getHost() + ":" + httpPort();
    }

    /**
     * @return Authenticated Git URL ("http://username:token@host:port/username/repo.git")
     */
    public String repoUrl(String projectPath, String token) {
        String username = projectPath.split("/")[0];
        return "http://" + username + ":" + token + "@" + httpHost() + ":" + httpPort() + "/" + projectPath + ".git";
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

    /**
     * Call {@link #start()} instead to ensure Selenium does not timeout.
     * @throws RuntimeException whenever this method is called
     * @deprecated use {@link #start()} instead.
     */
    @Override
    @Deprecated
    public void start() {
        throw new RuntimeException("do not call start, call start(CapybaraPortingLayer)");
    }

    @SuppressWarnings("resource")
    public void start(CapybaraPortingLayerImpl p) {
        // Startup takes too long which causes the selenium session to timeout so consumers must call waitForReady
        // which polls using webdriver. But the default will timeout after 60 seconds so we need to say don't wait
        // which means if things do fail we do not get logs :(
        // so make this a no-op
        waitingFor(new WaitStrategy() {

            Duration timeout = READINESS_TIMEOUT;

            @Override
            public void waitUntilReady(WaitStrategyTarget waitStrategyTarget) {
                try {
                    GitLabPage gitLabPage = new GitLabPage(p.injector, new URL(getHttpUrl()));
                    p.waitFor(this)
                            .withMessage("Waiting for GitLab to come up")
                            .withTimeout(timeout)
                            .pollingEvery(READINESS_POLL_INTERVAL)
                            .ignoring(WebDriverException.class)
                            .until(() -> {
                                gitLabPage.open();
                                return gitLabPage.isReady();
                            });
                } catch (MalformedURLException e) {
                    throw new ContainerLaunchException("GitLab URL is invalid", e);
                } catch (org.openqa.selenium.TimeoutException te) {
                    throw new ContainerLaunchException("Timed out waiting for container to become healthy", te);
                }
            }

            @Override
            public WaitStrategy withStartupTimeout(Duration timeout) {
                this.timeout = timeout;
                return this;
            }
        });
        super.start();
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
