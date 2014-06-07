package org.jenkinsci.test.acceptance.server;

import com.cloudbees.sdk.extensibility.ExtensionList;
import com.google.inject.Injector;
import jnr.unixsocket.UnixServerSocketChannel;
import jnr.unixsocket.UnixSocketAddress;
import jnr.unixsocket.UnixSocketChannel;
import org.jenkinsci.test.acceptance.FallbackConfig;
import org.jenkinsci.test.acceptance.controller.JenkinsController;
import org.jenkinsci.test.acceptance.controller.JenkinsControllerFactory;
import org.jenkinsci.test.acceptance.guice.World;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.channels.Channels;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.SynchronousQueue;

/**
 * Pre-launch {@link JenkinsController} so that tests can use them without waiting.
 *
 * <p>
 * This is intended to run in another JVM from the test harness, so that it can have independent lifecycle.
 * During development of tests, a test VM would come and go quickly. This design helps them maintain efficiency
 * in such a situation.
 *
 * @see docs/PRELAUNCH.md
 * @author Kohsuke Kawaguchi
 */
public class JenkinsControllerPoolProcess {
    @Inject
    ExtensionList<JenkinsControllerFactory> factories;

    @Inject
    Injector injector;

    private BlockingQueue<JenkinsController> queue;

    @Option(name="-n",usage="Number of instances to pool. >=1.")
    public int n = Integer.getInteger("count",1);

    @Option(name="-socket",usage="Unix domain socket file to communicate with client")
    public File socket = SOCKET;


    public static void main(String[] args) throws Exception {
        MAIN = true;
        JenkinsControllerPoolProcess proc = new JenkinsControllerPoolProcess();
        CmdLineParser p = new CmdLineParser(proc);
        try {
            p.parseArgument(args);
            proc.run();
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            System.err.println("Usage: java -jar TODO.jar ...options...");
            p.printUsage(System.err);
        }
    }

    public void run() throws Exception {
        // there's always one process that's waiting to be in the queue,
        // so the actual length of the queue has to be n-1.
        if (n==1)
            queue = new SynchronousQueue<>();
        else
            queue = new LinkedBlockingDeque<>(n-1);

        World w = World.get();
        w.getInjector().injectMembers(this);

        new Thread() {
            /**
             * Just keeps on creating new controllers and put it into the queue.
             * Because queue is blocking, this will only prelaunch up to 3.
             */
            @Override
            public void run() {
                try {
                    FallbackConfig f = new FallbackConfig();
                    while (true) {
                        JenkinsController c = f.createController(injector,factories);
                        queue.put(c);
                    }
                } catch (Throwable e) {
                    // fail fatally
                    e.printStackTrace();
                    System.exit(1);
                }
            }
        }.start();

        processServerSocket();

    }

    /**
     * Accepts connection to Unix domain socket and hand it off to a connection handling thread.
     */
    private void processServerSocket() throws IOException, InterruptedException {
        socket.deleteOnExit();
        socket.delete();

        try (UnixServerSocketChannel channel = UnixServerSocketChannel.open()) {
            channel.configureBlocking(true);
            channel.socket().bind(new UnixSocketAddress(socket));

            System.out.println("JUT Server is ready");

            while (true) {
                final UnixSocketChannel c = channel.accept();
                System.out.println("Accepted");
                final JenkinsController j = queue.take();
                System.out.println("Handed out "+j.getUrl());

                new Thread() {
                    @Override
                    public void run() {
                        processConnection(c, j);
                    }
                }.start();
            }
        }
    }

    /**
     * Serve individual connection to the test harness.
     */
    private void processConnection(UnixSocketChannel c, JenkinsController j) {
        try {
            try {
                try (
                    BufferedReader in = new BufferedReader(new InputStreamReader(Channels.newInputStream(c)));
                    PrintWriter out = new PrintWriter(Channels.newOutputStream(c),true)) {

                    out.println(j.getUrl());
                    String cmd;
                    while (null != (cmd = in.readLine())) {
                        switch (cmd.trim().toLowerCase()) {
                            case "start":
                                j.start();
                                break;
                            case "stop":
                                j.stop();
                                break;
                        }
                    }
                }
            } finally {
                System.out.println("done");
                j.stop();
                j.tearDown();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static final File SOCKET = new File(System.getProperty("user.home"),"jenkins.sock");
    /**
     * Are we running the JUT server?
     */
    public static boolean MAIN = false;
}
