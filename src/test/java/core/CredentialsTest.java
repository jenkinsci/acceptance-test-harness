package core;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.plugins.credentials.ManagedCredentials;
import org.jenkinsci.test.acceptance.plugins.ssh_credentials.SshPrivateKeyCredential;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Vivek Pandey
 */
public class CredentialsTest extends AbstractJUnitTest {
    @Test
    public void createSshKeys() {
        ManagedCredentials c = new ManagedCredentials(jenkins);

        String username = "xyz";
        String privKey = "1212121122121212";

        c.open();
        SshPrivateKeyCredential sc = c.add(SshPrivateKeyCredential.class);
        sc.username.set(username);
        sc.selectEnterDirectly().privateKey.set(privKey);
        c.save();

        //now verify
        jenkins.visit("credentials");
        Assert.assertEquals(find(by.input("_.username")).getAttribute("value"), username);
        Assert.assertEquals(find(by.input("_.privateKey")).getText(), privKey);
    }
}
