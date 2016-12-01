package org.jenkinsci.test.acceptance.docker;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jenkinsci.test.acceptance.junit.Resource;
import org.jenkinsci.utils.process.ProcessUtils;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import static java.lang.String.*;

/**
 * Running container, a virtual machine.
 *
 * @author Kohsuke Kawaguchi
 */
public class DockerContainer implements Closeable {
    private String cid;
    private Process p;
    private File logfile;
    private Thread shutdownHook;

    /* package */ void init(String cid, Process p, File logfile) {
        this.cid = cid;
        this.p = p;
        this.logfile = logfile;
        shutdownHook = new Thread() {
            @Override
            public void run() {
                shutdownHook = null;
                close();
            }
        };
        Runtime.getRuntime().addShutdownHook(shutdownHook);

        assertRunning();
    }

    public void assertRunning() {
        try {
            JsonNode state = inspect().get("State");
            if (!"running".equals(state.get("Status").asText())) {
                throw new Error("The container is not running: " + state.toString());
            }
        } catch (IOException e) {
            throw new Error("The container is not running", e);
        }
    }

    /**
     * By convention, docker fixtures put their resources into a sub-directory that has the same name as
     * the class name.
     */
    public Resource resource(String relativePath) {
        for (Class c = getClass(); c != null; c = c.getSuperclass()) {
            URL res = c.getResource(c.getSimpleName() + "/" + relativePath);
            if (res != null)
                return new Resource(res);
        }
        throw new IllegalArgumentException("No such resource " + relativePath + " in " + getClass());
    }

    public String getCid() {
        return cid;
    }

    public File getLogfile() {
        return logfile;
    }

    public int getPid() {
        return ProcessUtils.getPid(p);
    }

    /**
     * Finds the ephemeral ip that the given container port is bind to.
     */
    public String ipBound(int n) {
        assertRunning();
        try {
            if (sharingHostDockerService()) {
                return getIpAddress();
            }
            String out = Docker.cmd("port").add(cid, n).popen().verifyOrDieWith("docker port command failed").trim();
            if (out.isEmpty())  // expected to return single line like "0.0.0.0:55326"
                throw new IllegalStateException(format("Port %d is not mapped for container %s", n, cid));
            return out.split(":")[0];
        } catch (IOException | InterruptedException e) {
            throw new AssertionError("Failed to figure out port map " + n, e);
        }
    }

    /**
     * Finds the ephemeral port that the given container port is mapped to.
     */
    public int port(int n) {
        assertRunning();
        try {
            if (sharingHostDockerService()) {
                return n;
            }
            String out = Docker.cmd("port").add(cid, n).popen().verifyOrDieWith("docker port command failed").trim();
            if (out.isEmpty())  // expected to return single line like "0.0.0.0:55326"
                throw new IllegalStateException(format("Port %d is not mapped for container %s", n, cid));

            return Integer.parseInt(out.split(":")[1]);
        } catch (IOException | InterruptedException e) {
            throw new AssertionError("Failed to figure out port map " + n, e);
        }
    }

    /**
     * Stops and remove any trace of the container
     */
    public void close() {
        try {
            p.destroy();
            // If container fail to start, this produces phone failure that presents container to be removed
            int killStatus = Docker.cmd("kill").add(cid).build().inheritIO().start().waitFor();
            Docker.cmd("rm").add(cid)
                    .popen().verifyOrDieWith("Failed to rm " + cid + ". kill completed with " + killStatus);
            if (shutdownHook != null) {
                Runtime.getRuntime().removeShutdownHook(shutdownHook);
            }
        } catch (IOException | InterruptedException e) {
            throw new AssertionError("Failed to close down docker container " + cid, e);
        }
    }

    /**
     * Copies a file or folder from inside the container to the outside. Silently overwrites an existing file.
     *
     * @param from the absolute path of the resource to copy
     * @param toPath the absolute path of the destination directory
     * @return {@code true}  if the copy was a success otherwise {@code false}
     */
    public boolean cp(String from, String toPath) {
        assertRunning();
        File srcFile = new File(from);
        String fileName = srcFile.getName();
        File destFile = new File(toPath, fileName);
        if (destFile.exists()) {
            destFile.delete();
        }
        try {
            Docker.cmd("cp").add(cid + ":" + from).add(new File(toPath))
                    .popen().verifyOrDieWith(format("Failed to copy %s to %s", from, toPath));
        } catch (IOException | InterruptedException e) {
            return false;
        }

        assertRunning();

        return destFile.exists();
    }

    /**
     * Provides details of this container.
     */
    public JsonNode inspect() throws IOException {
        return new ObjectMapper().readTree(Docker.cmd("inspect").add(cid).popen().withErrorCheck()).get(0);
    }

    /**
     * IP address of this container reachable through the bridge.
     */
    public String getIpAddress() throws IOException {
        return inspect().get("NetworkSettings").get("IPAddress").asText();
    }

    @Override
    public String toString() {
        return "Docker container " + cid;
    }
    
    /**
     * Support the case when ATHs are running in a docker container and
     * using the host docker service to spin "sibling" containers, as
     * described in http://jpetazzo.github.io/2015/09/03/do-not-use-docker-in-docker-for-ci/
     */
    public boolean sharingHostDockerService() {
        return Boolean.valueOf(System.getenv("SHARED_DOCKER_SERVICE"));
    }
}
