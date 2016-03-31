package org.jenkinsci.test.acceptance.docker;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;

/**
 * A DockerContainer whose <code>Dockerfile</code> is not static but requires some knowledge of the environment it is going to run in.
 *
 * Before the image is created {@link #process(String)} will be called which can be used to replace place holders with given values or manipulate the docker file in anyother way.
 */
public class DynamicDockerContainer extends DockerContainer {

    /**
     * Reads the contents from the given file and passes the result to {@link #process(String)}, the result of which is then written back into the file.
     * @param f the Dockerfile
     * @throws IOException 
     */
    void process(File f) throws IOException {
        String contents = FileUtils.readFileToString(f, StandardCharsets.UTF_8);
        contents = process(contents);
        FileUtils.write(f, contents, StandardCharsets.UTF_8);
    }

    /**
     * Manipulates the provided Dockerfile contents.
     * This method replaces any occurrence of <code>@@DOCKER_HOST@@<code> with the ip address of the machine running docker.
     * @param contents the original contents of the Dockerfile
     * @return a String with the new contents of the Dockerfile
     */
    protected String process(String contents) {
        return contents.replaceAll("@@DOCKER_HOST@@", DockerImage.getDockerHost());
    }
}
