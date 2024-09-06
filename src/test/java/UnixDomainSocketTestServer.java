import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import jnr.unixsocket.UnixServerSocketChannel;
import jnr.unixsocket.UnixSocketAddress;
import jnr.unixsocket.UnixSocketChannel;
import org.apache.commons.io.IOUtils;

/**
 * Unix domain socket test.
 *
 * @author Kohsuke Kawaguchi
 */
public class UnixDomainSocketTestServer {
    public static void main(String[] args) throws Exception {
        File path = new File("./jenkins.sock");
        path.deleteOnExit();
        UnixServerSocketChannel channel = UnixServerSocketChannel.open();
        channel.configureBlocking(true);
        channel.socket().bind(new UnixSocketAddress(path));

        while (true) {
            UnixSocketChannel c = channel.accept();
            System.out.println("Accepted");

            InputStream in = Channels.newInputStream(c);
            OutputStream out = Channels.newOutputStream(c);

            IOUtils.copy(in, out);
            System.out.println("Closed");
            c.close();
        }
    }
}
