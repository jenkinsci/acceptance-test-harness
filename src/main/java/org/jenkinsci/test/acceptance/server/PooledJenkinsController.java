package org.jenkinsci.test.acceptance.server;

import static java.lang.System.getenv;
import static java.lang.System.in;
import static java.lang.System.out;

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
        if (w!=null)
            w.close();
        if (r!=null)
            r.close();
        if (channel!=null)
            channel.close();
        channel = null;
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
