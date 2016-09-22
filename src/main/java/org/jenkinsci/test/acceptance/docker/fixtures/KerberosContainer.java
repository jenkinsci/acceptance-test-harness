/*
 * The MIT License
 *
 * Copyright (c) Red Hat, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.jenkinsci.test.acceptance.docker.fixtures;

import org.jenkinsci.test.acceptance.docker.Docker;
import org.jenkinsci.test.acceptance.docker.DockerFixture;
import org.jenkinsci.test.acceptance.docker.DynamicDockerContainer;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;


/**
 * Kerberos kdc server ready to authenticate user principal 'user@EXAMPLE.COM' and service 'HTTP/<LOCAL_HOSTNAME>@EXAMPLE.COM'.
 *
 * @author ogondza.
 */
@DockerFixture(id="kerberos",ports={88, 749})
public class KerberosContainer extends DynamicDockerContainer {

    private static final String HOST_FQDN;
    static {
        String hostFqdn = null;
        try {
            hostFqdn = InetAddress.getByAddress(new byte[]{127, 0, 0, 1}).getCanonicalHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            throw new Error(e);
        } finally {
            HOST_FQDN = hostFqdn;
        }
    }

    private File targetDir = null;
    private File loginConf = null;
    private File krb5Conf = null;

    public File populateTargetDir(File target) {
        // No ned to do this twice
        if (targetDir == null) {
            targetDir = target;

            // Get the keytabs out of the container
            cp("/target/keytab/", target.getAbsolutePath());
            if (!new File(targetDir, "keytab/service").exists()) throw new AssertionError("Service keytab not created");
            if (!new File(targetDir, "keytab/user").exists()) throw new AssertionError("User keytab not created");

            // Generate plugin configuration files
            loginConf = new File(targetDir, "login.conf");
            try (FileWriter fw = new FileWriter(loginConf)) {
                fw.write(resource("src/login.conf").asText()
                        .replaceAll("__SERVICE_KEYTAB__", getServiceKeytab())
                        .replaceAll("__HOST_NAME__", HOST_FQDN)
                );
            } catch (IOException e) {
                throw new Error(e);
            }

            krb5Conf = new File(targetDir, "krb5.conf");
            try (FileWriter fw = new FileWriter(krb5Conf)) {
                fw.write(resource("src/etc.krb5.conf").asText()
                        .replaceAll("__KDC_PORT__", String.valueOf(port(88)))
                        .replaceAll("__ADMIN_PORT__", String.valueOf(port(749)))
                );
            } catch (IOException e) {
                throw new Error(e);
            }
        }
        return targetDir;
    }

    public String getLoginConfPath() {
        return loginConf.getAbsolutePath();
    }

    public String getKrb5ConfPath() {
        return krb5Conf.getAbsolutePath();
    }

    /**
     * Keytab to be linked with the service - Jenkins with Kerberos SSO.
     *
     * This is copied from the container.
     */
    public String getServiceKeytab() {
        return new File(targetDir, "keytab/service").getAbsolutePath();
    }

    /**
     * Keytab used so we do not have to enter password to get ticket granting ticket.
     *
     * This is copied from the container.
     */
    public String getUserKeytab() {
        return new File(targetDir, "keytab/user").getAbsolutePath();
    }

    /**
     * Temporary, test specific token cache for client.
     */
    public String getClientKeytab() {
        return new File(targetDir, "keytab/client_tmp").getAbsolutePath();
    }

    @Override // TODO Can be replaced by build args that would require more flexible build customization to be implemented
    protected String process(String contents) {
        return contents.replace("${HOST_NAME}", HOST_FQDN);
    }
}
