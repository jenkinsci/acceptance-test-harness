package org.jenkinsci.test.acceptance.server;

import com.cloudbees.sdk.extensibility.ExtensionList;
import jnr.unixsocket.UnixServerSocketChannel;
import jnr.unixsocket.UnixSocketAddress;
import jnr.unixsocket.UnixSocketChannel;
import org.jenkinsci.test.acceptance.FallbackConfig;
import org.jenkinsci.test.acceptance.controller.ControllerFactory;
import org.jenkinsci.test.acceptance.controller.JenkinsController;
import org.jenkinsci.test.acceptance.guice.TestCleaner;
import org.jenkinsci.test.acceptance.guice.World;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.channels.Channels;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;

/**
 * Pre-launch {@link JenkinsController} so that tests can use them without waiting.
 *
 * <p>
 * This is intended to run in another JVM from the test harness, so that it can have independent lifecycle.
 * During development of tests, a test VM would come and go quickly. This design helps them maintain efficiency
 * in such a situation.
 *
 * @author Kohsuke Kawaguchi
 */
public class JenkinsControllerPoolProcess {
    @Inject
    ExtensionList<ControllerFactory> factories;

    TestCleaner cleaner = new TestCleaner();

    private final BlockingQueue<JenkinsController> queue = new SynchronousQueue<>(); // new LinkedBlockingQueue<>(0);

    public static void main(String[] args) throws Exception {
        new JenkinsControllerPoolProcess().run();
    }

    public void run() throws Exception {
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
                        JenkinsController c = f.createController(factories, cleaner);
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
     * Acccepts connection to Unix domain socket and hand it off to a connection handling thread.
     */
    private void processServerSocket() throws IOException, InterruptedException {
        SOCKET.deleteOnExit();
        SOCKET.delete();

        try (UnixServerSocketChannel channel = UnixServerSocketChannel.open()) {
            channel.configureBlocking(true);
            channel.socket().bind(new UnixSocketAddress(SOCKET));

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
                    while (true) {
                        String cmd = in.readLine();
                        if (cmd==null)  return;

                        // TODO: implement restart
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
}
