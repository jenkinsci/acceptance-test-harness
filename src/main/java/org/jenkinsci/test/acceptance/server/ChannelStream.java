package org.jenkinsci.test.acceptance.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

/**
 * Work around the lock problem in {@link Channels#newInputStream(ReadableByteChannel)}
 * and {@link Channels#newOutputStream(WritableByteChannel)} that happens when two threads
 * try to read/write at the same time.
 *
 * <p>
 * UNIX domain socket doesn't handle half-close, so defer the actual close down to a later point.
 *
 * @author Kohsuke Kawaguchi
 */
class ChannelStream {
    public static InputStream in(final ReadableByteChannel ch) throws IOException {
        return Channels.newInputStream(new ReadableByteChannel() {
            @Override
            public int read(ByteBuffer dst) throws IOException {
                return ch.read(dst);
            }

            @Override
            public void close() throws IOException {
                //                ch.close();
            }

            @Override
            public boolean isOpen() {
                return ch.isOpen();
            }
        });
    }

    public static OutputStream out(final WritableByteChannel ch) throws IOException {
        return Channels.newOutputStream(new WritableByteChannel() {
            @Override
            public int write(ByteBuffer src) throws IOException {
                return ch.write(src);
            }

            @Override
            public void close() throws IOException {
                //                ch.close();
            }

            @Override
            public boolean isOpen() {
                return ch.isOpen();
            }
        });
    }
}
