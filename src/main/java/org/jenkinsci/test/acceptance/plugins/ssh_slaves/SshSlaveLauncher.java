package org.jenkinsci.test.acceptance.plugins.ssh_slaves;

import org.jenkinsci.test.acceptance.plugins.credentials.UserPwdCredential;
import org.jenkinsci.test.acceptance.plugins.ssh_credentials.SshCredentialDialog;
import org.jenkinsci.test.acceptance.plugins.ssh_credentials.SshPrivateKeyCredential;
import org.jenkinsci.test.acceptance.po.ComputerLauncher;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.PageObject;

/**
 * @author Kohsuke Kawaguchi
 */
@Describable("hudson.plugins.sshslaves.SSHLauncher")
public class SshSlaveLauncher extends ComputerLauncher {
    public final Control host = control("host");
    public final Control credentialsId = control("credentialsId");
    public final Control prefixCmd = control("prefixStartSlaveCmd");
    public final Control suffixCmd = control("suffixStartSlaveCmd");
    public final Control timeout = control("launchTimeoutSeconds");
    public final Control retries = control("maxNumRetries");
    public final Control javaPath = control("javaPath");
    public final Control jvmOptions = control("jvmOptions");
        
    public SshSlaveLauncher(PageObject context, String path) {
        super(context, path);
    }

    public SshCredentialDialog addCredential() {
        self().findElement(by.button("Add")).click();

        return new SshCredentialDialog(getPage(), "/credentials");
    }

    public SshSlaveLauncher port(int port) {
        ensureAdvancedOpen();
        control("port").set(port);
        return this;
    }

    private void ensureAdvancedOpen() {
        control("advanced-button").click();
    }
    
    /**
     * Add username/password based credentials to the configuration
     * 
     * @param username to use
     * @param password for the username
     * @return the SshSlaveLauncher to be configured
     */
    public SshSlaveLauncher pwdCredentials(String username, String password) {
        SshCredentialDialog dia = this.addCredential();
        UserPwdCredential cred = dia.select(UserPwdCredential.class);
        cred.username.set(username);
        cred.password.set(password);
        cred.add();
        return this;
    }

    /**
     * Add username/key based credentials to the configuration
     * 
     * @param username to use
     * @param key for the private key to use
     * @return the SshSlaveLauncher to be configured
     */
    public SshSlaveLauncher keyCredentials(String username, String key) {
        SshCredentialDialog dia = this.addCredential();
        SshPrivateKeyCredential cred = dia.select(SshPrivateKeyCredential.class);
        cred.username.set(username);
        cred.enterDirectly(key);
        cred.add();
        return this;
    }
}
