package org.jenkinsci.test.acceptance.docker;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DockerImageTest {

    @Mock
    DockerImage.DockerHostResolver dockerHostResolver;

    @Test
    public void shouldReturnLocalhostIfDockerHostEnvironmentNotSet() {
        Mockito.when(dockerHostResolver.getDockerHostEnvironmentVariable()).thenReturn(null);
        DockerImage dockerImage = new DockerImage("a");
        dockerImage.dockerHostResolver = dockerHostResolver;

        Assert.assertThat(dockerImage.getDockerHost(), CoreMatchers.is("127.0.0.1"));
    }

    @Test
    public void shouldReturnIpFromDockerHostEnvironmentVariable() {
        Mockito.when(dockerHostResolver.getDockerHostEnvironmentVariable()).thenReturn("tcp://192.168.99.100:2376");
        DockerImage dockerImage = new DockerImage("a");
        dockerImage.dockerHostResolver = dockerHostResolver;

        Assert.assertThat(dockerImage.getDockerHost(), CoreMatchers.is("192.168.99.100"));
    }

    @Test
    public void shouldReturnLocalhostInCaseOfInvalidUri() {
        Mockito.when(dockerHostResolver.getDockerHostEnvironmentVariable()).thenReturn("hfdsdfah");
        DockerImage dockerImage = new DockerImage("a");
        dockerImage.dockerHostResolver = dockerHostResolver;

        Assert.assertThat(dockerImage.getDockerHost(), CoreMatchers.is("127.0.0.1"));
    }

    @Test
    public void shouldReturnLocalhostInCaseOfUnixSocket() {
        Mockito.when(dockerHostResolver.getDockerHostEnvironmentVariable()).thenReturn("unix:///var/run/docker.sock");
        DockerImage dockerImage = new DockerImage("a");
        dockerImage.dockerHostResolver = dockerHostResolver;

        Assert.assertThat(dockerImage.getDockerHost(), CoreMatchers.is("127.0.0.1"));
    }

}
