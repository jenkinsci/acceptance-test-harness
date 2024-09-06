package org.jenkinsci.test.acceptance.po;

/**
 * Page object for logging out of Jenkins.
 * @author Marco.Miller@ericsson.com
 */
public class Logout extends PageObject {
    public Logout(Jenkins jenkins) {
        super(jenkins.injector, jenkins.url("logout"));
    }
}
