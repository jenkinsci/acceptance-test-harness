package org.jenkinsci.test.acceptance.server;

import com.cloudbees.sdk.extensibility.ExtensionList;
import com.google.inject.Injector;
import hudson.remoting.Channel;
import hudson.remoting.Channel.Mode;
import hudson.remoting.ChannelBuilder;
import jnr.unixsocket.UnixServerSocketChannel;
import jnr.unixsocket.UnixSocketAddress;
import jnr.unixsocket.UnixSocketChannel;
import org.jenkinsci.test.acceptance.FallbackConfig;
import org.jenkinsci.test.acceptance.controller.IJenkinsController;
import org.jenkinsci.test.acceptance.controller.JenkinsController;
import org.jenkinsci.test.acceptance.controller.JenkinsControllerFactory;
import org.jenkinsci.test.acceptance.guice.TestCleaner;
import org.jenkinsci.test.acceptance.guice.TestLifecycle;
import org.jenkinsci.test.acceptance.guice.World;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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

    @Inject
    TestLifecycle lifecycle;

    private BlockingQueue<QueueItem> queue;

    @Option(name="-n",usage="Number of instances to pool. >=1.")
    public int n = Integer.getInteger("count",1);

    @Inject @Named("socket")
    public File socket;

    private final ExecutorService executors = Executors.newCachedThreadPool();

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
                        lifecycle.startTestScope();
                        JenkinsController c = f.createController(injector,factories);
                        c.start();
                        queue.put(new QueueItem(c,lifecycle.export()));
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

            System.out.println("JUT Server is ready and listening at " + socket.getAbsolutePath());

            while (true) {
                final UnixSocketChannel c = channel.accept();
                System.out.println("Accepted");
                final QueueItem qi = queue.take();
                final JenkinsController j = qi.controller;
                System.out.println("Handed out "+j.getUrl());

                new Thread("Connection handling thread") {
                    @Override
                    public void run() {
                        lifecycle.import_(qi.testScope);
                        try {
                            processConnection(c, j);
                        } finally {
                            TestCleaner scope = injector.getInstance(TestCleaner.class);
                            if (scope!=null)
                                scope.performCleanUp();
                            lifecycle.endTestScope();
                        }
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
                    InputStream in = ChannelStream.in(c);
                    OutputStream out = ChannelStream.out(c)) {

                    Channel ch = new ChannelBuilder(j.getLogId(), executors).withMode(Mode.BINARY).build(in, out);
                    ch.setProperty("controller", ch.export(IJenkinsController.class,j));

                    // wait for the connection to be shut down
                    ch.join();
                }
            } finally {
                System.out.println("done");
                j.stop();
                j.tearDown();
                c.close();
            }
        } catch (IOException|InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Are we running the JUT server?
     */
    public static boolean MAIN = false;

    static class QueueItem {
        final JenkinsController controller;
        final Map testScope;

        QueueItem(JenkinsController controller, Map testScope) {
            this.controller = controller;
            this.testScope = testScope;
        }
    }
}
