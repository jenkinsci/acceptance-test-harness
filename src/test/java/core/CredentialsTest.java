package core;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.po.SshPrivateKeyCredential;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Vivek Pandey
 */
public class CredentialsTest extends AbstractJUnitTest {
    @Test
    public void createSshKeys(){
        SshPrivateKeyCredential c = new SshPrivateKeyCredential(jenkins);

        String username = "xyz";
        String privKey = "1212121122121212";
        c.create("GLOBAL", username, privKey);
        //now verify
        jenkins.visit("credentials");
        Assert.assertEquals(jenkins.find(by.input("_.username")).getAttribute("value"), username);
        Assert.assertEquals(jenkins.find(by.input("_.privateKey")).getText(), privKey);
    }
}
