package org.jenkinsci.test.acceptance;

import com.google.common.io.Files;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.trilead.ssh2.Connection;
import org.jclouds.domain.LoginCredentials;

import java.io.File;
import java.io.IOException;

import static com.google.common.base.Charsets.UTF_8;

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
        this.credentials = getLoginForCommandExecution(user, keyPair.privateKey);
    }

    @Override
    public void authenticate(Connection connection) throws IOException {
        String u = credentials.getUser();
        if(!connection.authenticateWithPublicKey(u, credentials.getPrivateKey().toCharArray(), credentials.getPassword())){
            throw new IOException(String.format("Public key authentication failed: trying to login as %s@%s with %s",
                    u, connection.getHostname(), keyPair.privateKey));
        }
    }

    private  LoginCredentials getLoginForCommandExecution(String user, File privateKeyFile){
        try {
            String privateKey = Files.toString(
                    privateKeyFile, UTF_8);
            return LoginCredentials.builder().
                    user(user).privateKey(privateKey).build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
