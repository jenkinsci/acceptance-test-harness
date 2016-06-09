package org.jenkinsci.test.acceptance.docker;

import com.google.inject.Inject;
import hudson.remoting.Which;
import org.apache.commons.io.FileUtils;
import org.jenkinsci.test.acceptance.utils.SHA1Sum;
import org.jenkinsci.utils.process.CommandBuilder;
import org.jvnet.hudson.annotation_indexer.Index;

import javax.annotation.CheckForNull;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


/**
 * Entry point to the docker support.
 * <p/>
 * Use this subsystem by injecting this class into your test.
 *
 * @author Kohsuke Kawaguchi
 * @author asotobueno
 */
@Singleton
public class Docker {

    /**
     * Command to invoke docker.
     */
    @Inject(optional = true)
    @Named("docker")
    private static String stringDockerCmd = "docker";

    private static List<String> dockerCmd;// = Arrays.asList("docker");

    @Inject(optional = true)
    public ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

    public Docker() {
        dockerCmd = Arrays.asList(stringDockerCmd);
    }

    public static CommandBuilder cmd(String cmd) {
        return new CommandBuilder(dockerCmd).add(cmd);
    }

    /**
     * Checks if docker is available on this system.
     */
    public boolean isAvailable() {
        try {
            return cmd("ps").popen().waitFor() == 0;
        } catch (InterruptedException | IOException e) {
            return false;
        }
    }

    /**
     * Builds a docker image.
     *
     * @param image Name of the image to be built.
     * @param dir   Directory that contains Dockerfile
     */
    public DockerImage build(String image, File dir) throws IOException, InterruptedException {
        return build(image, dir, null);
    }

    /**
     * Builds a docker image.
     *
     * @param image Name of the image to be built.
     * @param dir   Directory that contains Dockerfile
     * @param log   Log file to store image building output
     */
    public DockerImage build(String image, File dir, @CheckForNull File log) throws IOException, InterruptedException {
        // compute tag from the content of Dockerfile
        String tag = getDockerFileHash(dir);
        String full = image + ":" + tag;

        // check if the image already exists
        if (cmd("images").add(image).popen().verifyOrDieWith("failed to query the status of the image").trim().contains(" " + tag + " ")) {
            return new DockerImage(full);
        }

        CommandBuilder buildCmd = cmd("build").add("-t", full, dir);
        ProcessBuilder processBuilder = buildCmd.build();
        if (log != null) {
            processBuilder.redirectError(log).redirectOutput(log);
        } else {
            processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT).redirectOutput(ProcessBuilder.Redirect.INHERIT);
        }

        StringBuilder sb = new StringBuilder("Building Docker image `").append(buildCmd.toString()).append("`");
        if (log != null) {
            sb.append(": logfile is at ").append(log);
        }
        System.out.println(sb.toString());

        if (processBuilder.start().waitFor() != 0) {
            throw new Error("Failed to build image: " + tag);
        }
        return new DockerImage(full);
    }

    public DockerImage build(Class<? extends DockerContainer> fixture) throws IOException, InterruptedException {
        return build(fixture, null);
    }

    public DockerImage build(Class<? extends DockerContainer> fixture, File log) throws IOException, InterruptedException {
        if (fixture.getSuperclass() != DockerContainer.class && fixture.getSuperclass() != DynamicDockerContainer.class) {
            build((Class) fixture.getSuperclass()); // build the base image first
        }

        try {
            DockerFixture f = fixture.getAnnotation(DockerFixture.class);
            if (f == null) {
                throw new AssertionError(fixture + " is missing @DockerFixture");
            }

            File dir = File.createTempFile("Dockerfile", "dir");
            dir.delete();
            dir.mkdirs();
            try {
                copyDockerfileDirectory(fixture, f, dir);
                return build("jenkins/" + f.id(), dir, log);
            } finally {
                FileUtils.deleteDirectory(dir);
            }
        } catch (InterruptedException | IOException e) {
            throw new IOException("Failed to build image: " + fixture, e);
        }
    }

    //package scope for testing purposes. Ideally we should encapsulate Docker interactions so they can be mocked
    // and call public method.
    void copyDockerfileDirectory(Class<? extends DockerContainer> fixture, DockerFixture f, File dir)
            throws IOException {
        String dockerfilePath = resolveDockerfileLocation(fixture, f);
        copyDockerfileDirectoryFromClasspath(fixture, dockerfilePath, dir);
    }

    private String resolveDockerfileLocation(Class<? extends DockerContainer> fixture, DockerFixture f) {
        String prefix = null;
        if(isSpecificDockerfileLocationSet(f)) {
            prefix = f.dockerfileFolder();
        } else {
            prefix = fixture.getName();
        }
        return prefix.replace('.', '/').replace('$', '/');
    }

    private void copyDockerfileDirectoryFromClasspath(Class<? extends DockerContainer> fixture, String dockerfileLocation, File dir) throws IOException {
        File jar = null;
        try {
            jar = Which.jarFile(fixture);
        } catch (IllegalArgumentException e) {
            // fall through
        }

        if (jar!=null && jar.isFile()) {
            // files are packaged into a jar/war. extract them
            dockerfileLocation += "/";
            copyDockerfileDirectoryFromPackaged(jar, dockerfileLocation, dir);
        } else {
            // Dockerfile is not packaged into a jar file, so copy locally
            copyDockerfileDirectoryFromLocal(dockerfileLocation, dir);
        }
        // if the fixture is dynamic (needs to know something about our environment then process it.
        if (DynamicDockerContainer.class.isAssignableFrom(fixture)) {
            try {
                DynamicDockerContainer newInstance = (DynamicDockerContainer) fixture.newInstance();
                newInstance.process(new File(dir, "Dockerfile"));
            }
            catch (InstantiationException | IllegalAccessException ex) {
                throw new IOException("Could not transfrom Dockerfile", ex);
            }
        }
    }

    private boolean isSpecificDockerfileLocationSet(DockerFixture f) {
        return !f.dockerfileFolder().isEmpty();
    }

    private void copyDockerfileDirectoryFromPackaged(File jar, String fixtureLocation, File outputDirectory) throws IOException {
        try (JarFile j = new JarFile(jar)) {
            Enumeration<JarEntry> e = j.entries();
            while (e.hasMoreElements()) {
                JarEntry je = e.nextElement();
                if (je.getName().startsWith(fixtureLocation)) {
                    File dst = new File(outputDirectory, je.getName().substring(fixtureLocation.length()));
                    if (je.isDirectory()) {
                        dst.mkdirs();
                    } else {
                        try (InputStream in = j.getInputStream(je)) {
                            FileUtils.copyInputStreamToFile(in, dst);
                        }
                    }
                }
            }
        }
    }

    private void copyDockerfileDirectoryFromLocal(String fixtureLocation, File outputDirectory) throws IOException {
        URL resourceDir = classLoader.getResource(fixtureLocation);
        copyFile(outputDirectory, resourceDir);
    }

    private void copyFile(File outputDirectory, URL resourceDir) throws IOException {
        File dockerFileDir;
        try {
            dockerFileDir = new File(resourceDir.toURI());
        } catch (URISyntaxException e) {
            dockerFileDir = new File(resourceDir.getPath());
        }
        FileUtils.copyDirectory(dockerFileDir, outputDirectory);
    }

    private String getDockerFileHash(File dockerFileDir) {
        File dockerFile = new File(dockerFileDir, "Dockerfile");
        SHA1Sum dockerFileHash = new SHA1Sum(dockerFile);
        return dockerFileHash.getSha1String().substring(0, 12);
    }

    /**
     * Finds a fixture class that has the specified ID.
     *
     * @see org.jenkinsci.test.acceptance.docker.DockerFixture#id()
     */
    public Class<? extends DockerContainer> findFixture(String id) throws IOException {
        for (Class<?> t : Index.list(DockerFixture.class, classLoader, Class.class)) {
            if (t.getAnnotation(DockerFixture.class).id().equals(id)) {
                return t.asSubclass(DockerContainer.class);
            }
        }
        throw new IllegalArgumentException("No such docker fixture found: " + id);
    }


}
