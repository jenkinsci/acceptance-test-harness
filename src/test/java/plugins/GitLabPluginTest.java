package plugins;

import jakarta.inject.Inject;
import org.jenkinsci.test.acceptance.docker.DockerContainerHolder;
import org.jenkinsci.test.acceptance.docker.fixtures.GitLabContainer;
import org.jenkinsci.test.acceptance.junit.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@WithDocker
@Category(DockerTest.class)
@WithPlugins("gitlab-plugin")
@WithCredentials(credentialType = WithCredentials.SSH_USERNAME_PRIVATE_KEY, values = {"gitlab-plugin"})
public class GitLabPluginTest extends AbstractJUnitTest {
    private static final String USERNAME = "gitplugin";

    @Inject
    DockerContainerHolder<GitLabContainer> gitLabServer;

    private GitLabContainer container;
    private String repoUrl;
    private String host;
    private int port;

    @Before
    public void init() {
        container = gitLabServer.get();
        repoUrl = container.getRepoUrl();
        host = container.host();
        port = container.port();

    }

    @Test
    public void dummy_test() {
        System.out.println(container.getRepoUrl());
        System.out.println(container.host());
        System.out.println(container.port());
    }
}
