package org.jenkinsci.test.acceptance.utils;

import java.io.*;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

/**
 * Creates SHA1 sums of a file
 */
public class SHA1Sum {
    private byte[] sha1 = null;
    private String sha1String = null;
    private File inFile = null;

    public SHA1Sum(File inFile) {
        if (inFile.isFile()) {
            if (inFile.canRead()) {
                this.inFile = inFile;
                try {
                    this.sha1 = this.createSha1(this.inFile);
                } catch (NoSuchAlgorithmException | IOException e) {
                    throw new IllegalStateException(e);
                }
                this.sha1String = this.convertByteToString(this.sha1);
            } else {
                throw new RuntimeException("You are trying to make a sha1sum of a read protected file!");
            }
        } else {
            throw new RuntimeException("You are trying to make a sha1sum a directory!");
        }
    }

    public String getSha1String() {
        return this.sha1String;
    }

    public byte[] getSha1ByteArray() {
        return this.sha1;
    }


    private String convertByteToString(byte[] inByte) {
        try (Formatter formatter = new Formatter()) {
            for (final byte b : inByte) {
                formatter.format("%02x", b);
            }
            return formatter.toString();
        }
    }

    private byte[] createSha1(File file) throws NoSuchAlgorithmException, IOException {
        int n = 0;
        byte[] buffer = new byte[8192];

        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        try (InputStream fis = Files.newInputStream(file.toPath())) {
            while (n != -1) {
                n = fis.read(buffer);
                if (n > 0) {
                    digest.update(buffer, 0, n);
                }
            }
            return digest.digest();
        }

    }

}
