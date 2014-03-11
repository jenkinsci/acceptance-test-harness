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
public interface Authenticator {

    /**
     * Authenticate over Ssh connection
     */
    public void authenticate(Connection connection);

    @Singleton
    public static class PublicKeyAuthenticator implements Authenticator{
        private final String user;

        @Inject
        public PublicKeyAuthenticator(@Named("user")String user) {
            this.user = user;
        }

        @Override
        public void authenticate(Connection connection) {
            LoginCredentials credentials = Ssh.getLoginForCommandExecution();
            try {
                connection.authenticateWithPublicKey(user,
                        credentials.getPrivateKey().toCharArray(),
                        credentials.getPassword());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

