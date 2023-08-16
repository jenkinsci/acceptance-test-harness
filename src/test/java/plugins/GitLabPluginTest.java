package plugins;

import jakarta.inject.Inject;
import org.jenkinsci.test.acceptance.docker.DockerContainerHolder;
import org.jenkinsci.test.acceptance.docker.fixtures.GitLabContainer;
import org.jenkinsci.test.acceptance.junit.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import okhttp3.Response;

import static org.junit.Assert.*;

@WithDocker
@Category(DockerTest.class)
@WithPlugins("gitlab-plugin")
@WithCredentials(credentialType = WithCredentials.SSH_USERNAME_PRIVATE_KEY, values = {"gitlabplugin", "/org/jenkinsci/test/acceptance/docker/fixtures/GitLabContainer"})
public class GitLabPluginTest extends AbstractJUnitTest {

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
        assertNotNull(container.getRepoUrl());
        assertTrue(container.getRepoUrl().contains("ssh://git@"));
        assertNotNull(container.host());
    }

    @Test
    public void createRepo() {
        //This sends a request to make a new repo in the gitlab server with the name "testrepo" + a random number (random number is there so the test can be run multiple times without failing)
        //TODO: This test fails if there is already a repo with the same name, make a delete repo method and delete repo after making it, then we can remove the random number and test should pass every time
        Response response = container.createRepo("testrepo"+(int)Math.floor(Math.random()*100));
        assertEquals(201, response.code()); // 201 means the repo was created successfully
    }
}
