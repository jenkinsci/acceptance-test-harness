package org.jenkinsci.test.acceptance.controller;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.trilead.ssh2.Connection;
import org.jclouds.domain.LoginCredentials;

import java.io.File;
import java.io.IOException;

/**
 * @author Vivek Pandey
 */
@Singleton
public class PublicKeyAuthenticator implements Authenticator {

    private final LoginCredentials credentials;

    @Inject
    public PublicKeyAuthenticator(@Named("user") String user, SshKeyPair keyPair) {
        this.credentials = Ssh.getLoginForCommandExecution(user, keyPair.privateKey);
    }

    @Override
    public void authenticate(Connection connection) throws IOException {
        connection.authenticateWithPublicKey(credentials.getUser(), credentials.getPrivateKey().toCharArray(), credentials.getPassword());
    }
}
