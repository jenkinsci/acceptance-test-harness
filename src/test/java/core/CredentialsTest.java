package core;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.plugins.credentials.ManagedCredentials;
import org.jenkinsci.test.acceptance.plugins.ssh_credentials.SshPrivateKeyCredential;
import org.jenkinsci.test.acceptance.plugins.credentials.UserPwdCredential;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Vivek Pandey
 */
public class CredentialsTest extends AbstractJUnitTest {
    @Test
    public void createSshKeys() {
        final ManagedCredentials c = new ManagedCredentials(jenkins);

        final String username = "xyz";
        final String privKey = "1212121122121212";

        c.open();
        final SshPrivateKeyCredential sc = c.add(SshPrivateKeyCredential.class);
        sc.username.set(username);
        sc.selectEnterDirectly().privateKey.set(privKey);
        c.save();

        //now verify
        jenkins.visit("credentials");
        Assert.assertEquals(find(by.input("_.username")).getAttribute("value"), username);
        Assert.assertEquals(find(by.input("_.privateKey")).getText(), privKey);
    }

    @Test
    public void createUserPwd() {
        final ManagedCredentials c = new ManagedCredentials(jenkins);

        final String username = "xyz";
        final String password = "1212121122121212";

        c.open();
        final UserPwdCredential upc = c.add(UserPwdCredential.class);
        upc.username.set(username);
        upc.password.set(password);
        c.save();

        //now verify
        jenkins.visit("credentials");
        Assert.assertEquals(find(by.input("_.username")).getAttribute("value"), username);
    }
}
