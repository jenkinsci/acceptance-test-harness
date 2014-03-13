package org.jenkinsci.test.acceptance.controller;

import com.trilead.ssh2.Connection;

import java.io.IOException;

/**
 * @author Vivek Pandey
 */
public interface Authenticator {

    /**
     * Authenticate over Ssh connection
     */
    public void authenticate(Connection connection) throws IOException;
}

