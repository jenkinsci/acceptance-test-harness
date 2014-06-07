package org.jenkinsci.test.acceptance.server;

import com.cloudbees.sdk.extensibility.Extension;
import hudson.remoting.Channel;
import hudson.remoting.Channel.Mode;
import hudson.remoting.ChannelBuilder;
import jnr.unixsocket.UnixSocketAddress;
import jnr.unixsocket.UnixSocketChannel;
import org.jenkinsci.test.acceptance.controller.IJenkinsController;
import org.jenkinsci.test.acceptance.controller.JenkinsController;
import org.jenkinsci.test.acceptance.controller.LocalController.LocalFactoryImpl;
import org.jenkinsci.test.acceptance.log.LogListenable;
import org.jenkinsci.test.acceptance.log.LogListener;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import static java.lang.System.*;

/**
 * @author Kohsuke Kawaguchi
 */
public class PooledJenkinsController extends JenkinsController implements LogListenable {
    private URL url;
    private final File socket;
    private UnixSocketChannel conn;
    private List<LogListener> listeners = new ArrayList<>();
    private Channel channel;
    private IJenkinsController controller;

    public PooledJenkinsController(File socket) {
        this.socket = socket;
    }

    public PooledJenkinsController() {
        this(JenkinsControllerPoolProcess.SOCKET);
    }

    @Override
    public void addLogListener(LogListener l) {
        listeners.add(l);
    }

    @Override
    public void removeLogListener(LogListener l) {
        listeners.remove(l);
    }

    private boolean connect() throws IOException {
        if (conn !=null)      return false;

        UnixSocketAddress address = new UnixSocketAddress(socket);
        conn = UnixSocketChannel.open(address);

        channel = new ChannelBuilder("JenkinsPool", Executors.newCachedThreadPool())
                .withMode(Mode.BINARY)
                .build(Channels.newInputStream(conn), Channels.newOutputStream(conn));

        try {
            controller = (IJenkinsController)channel.waitForRemoteProperty(IJenkinsController.class);
            url = controller.getUrl();
        } catch (InterruptedException e) {
            throw new IOException(e);
        }

        return true;
    }

    @Override
    public void startNow() throws IOException {
        if (!connect()) {
            controller.start();
        }
    }

    @Override
    public void stopNow() throws IOException {
        controller.stop();
    }

    @Override
    public void populateJenkinsHome(File template, boolean clean) throws IOException {
        throw new UnsupportedOperationException("unsupported");
    }

    @Override
    public URL getUrl() {
        if (url==null)
            throw new IllegalStateException("This controller has not been started");
        return url;
    }

    @Override
    public void tearDown() throws IOException {
        channel.close();
        if (conn !=null)
            conn.close();
        conn = null;
    }

    @Override
    public void diagnose(Throwable cause) {
        // TODO: Report jenkins log
        cause.printStackTrace(out);
        if(getenv("INTERACTIVE") != null && getenv("INTERACTIVE").equals("true")){
            out.println("Commencing interactive debugging. Browser session was kept open.");
            out.println("Press return to proceed.");
            try {
                in.read();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Extension
    public static class FactoryImpl extends LocalFactoryImpl {
        @Override
        public String getId() {
            return "pool";
        }

        @Override
        public JenkinsController create() {
            return new PooledJenkinsController();
        }
    }
}
