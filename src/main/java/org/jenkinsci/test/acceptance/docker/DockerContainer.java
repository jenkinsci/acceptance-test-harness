package org.jenkinsci.test.acceptance.docker;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jenkinsci.test.acceptance.junit.Resource;
import org.jenkinsci.utils.process.ProcessUtils;

import java.io.File;
import java.io.IOException;

import static java.lang.String.*;

/**
 * Running container, a virtual machine.
 *
 * @author Kohsuke Kawaguchi
 */
public class DockerContainer {
    private String cid;
    private Process p;
    private File logfile;

    /*package*/ void init(String cid, Process p, File logfile) {
        this.cid = cid;
        this.p = p;
        this.logfile = logfile;
    }

    /**
     * By convention, docker fixtures put their resources into a sub-directory that has the same name as
     * the class name.
     */
    public Resource resource(String relativePath) {
        return new Resource(getClass().getResource(getClass().getSimpleName()+"/"+relativePath));
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
     * Finds the ephemeral port that the given container port is mapped to.
     */
    public int port(int n) {
        try {
            String out = Docker.cmd("port").add(cid, n).popen().verifyOrDieWith("docker port command failed").trim();
            if (out.isEmpty())  // expected to return single line like "0.0.0.0:55326"
                throw new IllegalStateException(format("Port %d is not mapped for container %s", n, cid));

            return Integer.parseInt(out.split(":")[1]);
        } catch (IOException|InterruptedException e) {
            throw new AssertionError("Failed to figure out port map "+n,e);
        }
    }

    /**
     * Stops and remove any trace of the container
     */
    public void clean() throws IOException, InterruptedException {
        p.destroy();
        Docker.cmd("kill").add(cid)
                .popen().verifyOrDieWith("Failed to kill " + cid);
        Docker.cmd("rm").add(cid)
                .popen().verifyOrDieWith("Failed to rm " + cid);
    }

    /**
     * Copies a files/folders from inside the container to outside
     */
    public void cp(String from, File to) throws IOException, InterruptedException {
        Docker.cmd("cp").add(cid+":"+from).add(to)
                .popen().verifyOrDieWith(format("Failed to copy %s to %s", from, to));
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
        return "Docker container "+cid;
    }
}
