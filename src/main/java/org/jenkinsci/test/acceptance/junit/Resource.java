package org.jenkinsci.test.acceptance.junit;

import org.apache.commons.io.IOUtils;

import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;

/**
 * Wraps a resource found by {@link Class#getResource(String)}.
 *
 * @author Kohsuke Kawaguchi
 */
public class Resource {
    public final URL url;

    public Resource(URL url) {
        this.url = url;
    }

    public InputStream asInputStream() {
        try {
            return url.openStream();
        } catch (IOException e) {
            throw new IOError(e);
        }
    }

    public Reader asReader() {
        try {
            return new InputStreamReader(url.openStream());
        } catch (IOException e) {
            throw new IOError(e);
        }
    }

    public String asText() {
        try {
            return IOUtils.toString(url.openStream());
        } catch (IOException e) {
            throw new IOError(e);
        }
    }

    public byte[] asByteArray() throws IOException {
        try (InputStream is = asInputStream()) {
            return IOUtils.toByteArray(is);
        }
    }
}
