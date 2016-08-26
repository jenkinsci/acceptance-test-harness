package org.jenkinsci.test.acceptance.docker;

import org.hamcrest.CoreMatchers;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DockerImageTest {

    private static final String DOCKER_HOST_IP= "42.42.42.42";
    private static final String DOCKER_HOST_SOCKET= "unix:///var/run/foo.sock";
    private static final String DOCKER_HOST_INVALID= "hfdsdfah";
    private static final String DOCKER_HOST_LOCALHOST= "127.0.0.1";

    @Mock
    DockerImage.DockerHostResolver dockerHostResolver;

    private static DockerImage.DockerHostResolver oldHostResolver;

    @BeforeClass
    public static void backup() {
        oldHostResolver = DockerImage.dockerHostResolver;
    }

    @AfterClass
    public static void restore() {
        DockerImage.dockerHostResolver = oldHostResolver;
    }


    @Test
    public void shouldReturnLocalhostIfDockerHostEnvironmentNotSet() {
        Mockito.when(dockerHostResolver.getDockerHostEnvironmentVariable()).thenReturn(null);
        DockerImage dockerImage = new DockerImage("a");
        dockerImage.dockerHostResolver = dockerHostResolver;

        Assert.assertThat(dockerImage.getDockerHost(), CoreMatchers.is("127.0.0.1"));
    }

    @Test
    public void shouldReturnIpFromDockerHostEnvironmentVariable() {
        Mockito.when(dockerHostResolver.getDockerHostEnvironmentVariable())
            .thenReturn("tcp://" + DOCKER_HOST_IP +  ":2376");
        DockerImage dockerImage = new DockerImage("a");
        dockerImage.dockerHostResolver = dockerHostResolver;

        Assert.assertThat(dockerImage.getDockerHost(), CoreMatchers.is(DOCKER_HOST_IP));
    }

    @Test
    public void shouldReturnLocalhostInCaseOfInvalidUri() {
        Mockito.when(dockerHostResolver.getDockerHostEnvironmentVariable()).thenReturn(DOCKER_HOST_INVALID);
        DockerImage dockerImage = new DockerImage("a");
        dockerImage.dockerHostResolver = dockerHostResolver;

        Assert.assertThat(dockerImage.getDockerHost(), CoreMatchers.is(DOCKER_HOST_LOCALHOST));
    }

    @Test
    public void shouldReturnLocalhostInCaseOfUnixSocket() {
        Mockito.when(dockerHostResolver.getDockerHostEnvironmentVariable()).thenReturn(DOCKER_HOST_SOCKET);
        DockerImage dockerImage = new DockerImage("a");
        dockerImage.dockerHostResolver = dockerHostResolver;

        Assert.assertThat(dockerImage.getDockerHost(), CoreMatchers.is(DOCKER_HOST_LOCALHOST));
    }

}
