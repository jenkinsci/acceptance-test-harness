package org.jenkinsci.test.acceptance.plugins.ssh_slaves;

import java.util.concurrent.TimeUnit;
import javax.annotation.CheckForNull;

import org.jenkinsci.test.acceptance.plugins.credentials.UserPwdCredential;
import org.jenkinsci.test.acceptance.plugins.ssh_credentials.SshCredentialDialog;
import org.jenkinsci.test.acceptance.plugins.ssh_credentials.SshPrivateKeyCredential;
import org.jenkinsci.test.acceptance.po.ComputerLauncher;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.PageObject;

import static org.junit.Assert.*;

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
    public final Control hostKeyVerificationStrategy = control("/");

    public SshSlaveLauncher(PageObject context, String path) {
        super(context, path);
    }

    public SshCredentialDialog addCredential() {

        find(by.button("Add")).click();

        if (getElement(by.xpath("//span[contains(@class,'credentials-add')]")) == null) {

            String providerXpathExpr = "//div[contains(@class,'credentials-add-menu-items')]"
                    + "/div[@class='bd']/ul[@class='first-of-type']/li[contains(@class, 'yuimenuitem')]"
                    + "/span[contains(@class,'yuimenuitemlabel') and contains(@title, 'Jenkins Credentials Provider')]";

            find(by.xpath(providerXpathExpr)).click();
        }
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
        final SshCredentialDialog dia = this.addCredential();
        final UserPwdCredential cred = dia.select(UserPwdCredential.class);
        cred.username.set(username);
        cred.password.set(password);
        //credentials are identified by their id. Set username as id so it can be found by it
        cred.setId(username);
        cred.add();
        waitForCredentialVisible(username);
        return this;
    }

    /**
     * Add username/password and id based credentials to the configuration
     *
     * @param username to use
     * @param password for the username
     * @id for unique identification
     * @return the SshSlaveLauncher to be configured
     */
    public SshSlaveLauncher pwdCredentials(String username, String password, String id) {
        final SshCredentialDialog dia = this.addCredential();
        final UserPwdCredential cred = dia.select(UserPwdCredential.class);
        cred.username.set(username);
        cred.password.set(password);
        //credentials are identified by their id.
        cred.setId(id);
        cred.add();
        waitForCredentialVisible(username);
        return this;
    }

    /**
     * Add username/key based credentials to the configuration
     *
     * @param username to use
     * @param key for the private key to use
     * @return the SshSlaveLauncher to be configured
     */
    public SshSlaveLauncher keyCredentials(String username, String key, @CheckForNull String passphrase) {
        final SshCredentialDialog dia = this.addCredential();
        final SshPrivateKeyCredential cred = dia.select(SshPrivateKeyCredential.class);
        cred.username.set(username);
        if (passphrase != null) {
            cred.passphrase.set(passphrase);
        }
        cred.enterDirectly(key);
        cred.add();
        waitForCredentialVisible(username);
        return this;
    }

    /**
     * Once a credential has been created for a given slave, this method can be used
     * to check whether it has already been rendered in the dropdown.
     */
    private void waitForCredentialVisible(final String credUsername) {
        assertTrue(waitFor().withTimeout(5, TimeUnit.SECONDS).until(() ->
                credentialsId.resolve().getText().contains(credUsername))
        );
        // Select the new credentials. Control.selectDropdownMenu seems to be YUI-only.
        selectCredentials(credUsername);
    }

    /**
     * Select the credentials to use by ID. The credentials need to be created before this method
     * is invoked, e.g. using the {@code @WithCredentials} annotation.
     *
     * @param credentialsId the ID of the credentials to use
     */
    public void selectCredentials(final String credentialsId) {
        this.credentialsId.select(credentialsId);
    }

    public void setSshHostKeyVerificationStrategy(Class<? extends SshHostKeyVerificationStrategy> type) {
        try {
            SshHostKeyVerificationStrategy strategy = type.newInstance();
            hostKeyVerificationStrategy.select(strategy.id());
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalArgumentException(type.getName() + " is missing a default constructor.", e);
        }

    }

    public abstract static class SshHostKeyVerificationStrategy {
        public abstract String id();
    }

    @Describable("hudson.plugins.sshslaves.verifiers.KnownHostsFileKeyVerificationStrategy")
    public static class KnownHostsFileKeyVerificationStrategy extends SshHostKeyVerificationStrategy {
        @Override
        public String id() {
            return "0";
        }
    }

    @Describable("hudson.plugins.sshslaves.verifiers.ManuallyProvidedKeyVerificationStrategy")
    public static class ManuallyProvidedKeyVerificationStrategy extends SshHostKeyVerificationStrategy {
        @Override
        public String id() {
            return "1";
        }
    }

    @Describable("hudson.plugins.sshslaves.verifiers.ManuallyTrustedKeyVerificationStrategy")
    public static class ManuallyTrustedKeyVerificationStrategy extends SshHostKeyVerificationStrategy {
        @Override
        public String id() {
            return "2";
        }
    }

    @Describable("hudson.plugins.sshslaves.verifiers.NonVerifyingKeyVerificationStrategy")
    public static class NonVerifyingKeyVerificationStrategy extends SshHostKeyVerificationStrategy {
        @Override
        public String id() {
            return "3";
        }
    }
}
