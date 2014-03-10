package org.jenkinsci.test.acceptance.server;

import com.cloudbees.sdk.extensibility.Extension;
import jnr.unixsocket.UnixSocketAddress;
import jnr.unixsocket.UnixSocketChannel;
import org.jenkinsci.test.acceptance.controller.JenkinsController;
import org.jenkinsci.test.acceptance.controller.LocalController.LocalFactoryImpl;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.channels.Channels;

/**
 * @author Kohsuke Kawaguchi
 */
public class PooledJenkinsController extends JenkinsController {
    private URL url;
    private final File socket;
    private UnixSocketChannel channel;
    private PrintWriter w;
    private BufferedReader r;

    public PooledJenkinsController(File socket) {
        this.socket = socket;
    }

    public PooledJenkinsController() {
        this(JenkinsControllerPoolProcess.SOCKET);
    }

    private boolean connect() throws IOException {
        if (channel!=null)      return false;

        UnixSocketAddress address = new UnixSocketAddress(socket);
        channel = UnixSocketChannel.open(address);
        w = new PrintWriter(Channels.newOutputStream(channel),true);
        r = new BufferedReader(new InputStreamReader(Channels.newInputStream(channel)));

        url = new URL(r.readLine());

        return true;
    }

    @Override
    public void startNow() throws IOException {
        if (!connect()) {
            w.println("start");
        }
    }

    @Override
    public void stopNow() throws IOException {
        w.println("stop");
    }

    @Override
    public URL getUrl() {
        return url;
    }

    @Override
    public void diagnose() {
    }

    @Override
    public void tearDown() throws IOException {
        w.close();
        r.close();
        channel.close();
        channel = null;
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
