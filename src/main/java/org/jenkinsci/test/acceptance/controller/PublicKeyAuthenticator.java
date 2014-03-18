package org.jenkinsci.test.acceptance.controller;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.trilead.ssh2.Connection;
import org.jclouds.domain.LoginCredentials;

import java.io.IOException;

/**
 * @author Vivek Pandey
 */
@Singleton
public class PublicKeyAuthenticator implements Authenticator {

    private final LoginCredentials credentials;
    private final SshKeyPair keyPair;

    @Inject
    public PublicKeyAuthenticator(@Named("user") String user, SshKeyPair keyPair) {
        this.keyPair = keyPair;
        this.credentials = Ssh.getLoginForCommandExecution(user, keyPair.privateKey);
    }

    @Override
    public void authenticate(Connection connection) throws IOException {
        String u = credentials.getUser();
        if(!connection.authenticateWithPublicKey(u, credentials.getPrivateKey().toCharArray(), credentials.getPassword())){
            throw new IOException(String.format("Public key authentication failed: trying to login as %s@%s with %s",
                    u, connection.getHostname(), keyPair.privateKey));
        }
    }
}
