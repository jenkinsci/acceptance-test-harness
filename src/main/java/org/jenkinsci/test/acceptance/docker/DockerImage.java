package org.jenkinsci.test.acceptance.docker;

import org.apache.commons.io.FileUtils;
import org.jenkinsci.utils.process.CommandBuilder;

import java.io.File;
import java.io.IOException;

/**
 * Container image, a template to launch virtual machines from.
 *
 * @author Kohsuke Kawaguchi
 */
public class DockerImage {
    public final String tag;

    public DockerImage(String tag) {
        this.tag = tag;
    }

    /**
     * Starts a container from this image.
     */
    public <T extends DockerContainer> T start(Class<T> type, int[] ports, CommandBuilder options, CommandBuilder cmd) throws InterruptedException, IOException {
        CommandBuilder docker = Docker.cmd("run").addAll(cmd.toList());
        for (int p : ports) {
            docker.add("-p","127.0.0.1::"+p);
        }

        File cid = File.createTempFile("docker", "cid");
        docker.add("--cidfile="+cid);

        if (options!=null)
            docker.addAll(options.toList());

        docker.add(tag);

        if (cmd!=null)
            docker.addAll(cmd.toList());

        File tmplog = File.createTempFile("docker", "log"); // initially create a log file here

        Process p = docker.build()
                .redirectInput(new File("/dev/null"))
                .redirectErrorStream(true)
                .redirectOutput(tmplog)
                .start();

        // TODO: properly wait for either cidfile to appear or process to exit
        Thread.sleep(1000);

        if (cid.exists()) {
            String id;
            do {
                Thread.sleep(500);
                id = FileUtils.readFileToString(cid);
            } while (id==null || id.length()==0);

            // rename the log file to match the container name
            File logfile = new File("/tmp/"+cid+".log");
            tmplog.renameTo(logfile);

            System.out.printf("Launching Docker container %s: logfile is at %s\n", cid, logfile);

            try {
                return type.getConstructor(String.class, Process.class, File.class).newInstance(id,p,logfile);
            } catch (ReflectiveOperationException e) {
                throw new AssertionError(e);
            }
        } else {
            try {
                p.exitValue();
                throw new IOException("docker died unexpectedly: "+docker+"\n"+FileUtils.readFileToString(tmplog));
            } catch (IllegalThreadStateException e) {
                throw new IOException("docker didn't leave CID file yet still running. Huh?: "+docker+"\n"+FileUtils.readFileToString(tmplog));
            }
        }
    }

    @Override
    public String toString() {
        return "DockerImage: "+tag;
    }
}
