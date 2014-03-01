package org.jenkinsci.test.acceptance.docker;

import com.google.inject.Inject;
import org.apache.commons.io.FileUtils;
import org.jenkinsci.utils.process.CommandBuilder;

import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Entry point to the docker support.
 *
 * @author Kohsuke Kawaguchi
 */
@Singleton
public class Docker {
    /**
     * Command to invoke docker.
     */
    @Inject(optional=true) @Named("docker")
    public static List<String> dockerCmd = Arrays.asList("sudo","docker");

    public static CommandBuilder cmd(String cmd) {
        return new CommandBuilder(dockerCmd).add(cmd);
    }

    /**
     * Checks if docker is available on this system.
     */
    public boolean isAvailable() {
        try {
            return cmd("help").popen().waitFor()==0;
        } catch (InterruptedException|IOException e) {
            return false;
        }
    }

    /**
     * Builds a docker image.
     *
     * @param tag
     *      Name of the image to be built.
     * @param dir
     *      A directory that contains Dockerfile
     */
    public DockerImage build(String tag, File dir) throws IOException, InterruptedException {
        if (cmd("build").add("-t", tag, ".").pwd(dir).system()!=0)
            throw new Error("Failed to build image: "+tag);
        return new DockerImage(tag);
    }

    /**
     * Starts a container of the specific fixture type.
     * This builds an image if need be.
     */
    public <T extends DockerContainer> T start(Class<T> fixture, CommandBuilder options, CommandBuilder cmd) throws IOException, InterruptedException {
        DockerFixture f = fixture.getAnnotation(DockerFixture.class);
        if (f==null)
            throw new AssertionError(fixture+" is missing @DockerFixture");

        File dockerfile = File.createTempFile("Dockerfile", "txt");
        try {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            FileUtils.copyURLToFile(cl.getResource(fixture.getName().replace('.', '/') + "/Dockerfile"),dockerfile);
            DockerImage img = build("jenkins/" + fixture.getName(), dockerfile);

            return img.start(fixture, f.ports(), options, cmd);
        } finally {
            dockerfile.delete();
        }

    }
}
