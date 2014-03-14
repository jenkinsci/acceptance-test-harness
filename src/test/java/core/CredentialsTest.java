package core;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.jenkinsci.test.acceptance.controller.JenkinsController;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.jenkinsci.test.acceptance.po.SshPrivateKeyCredential;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Vivek Pandey
 */
public class CredentialsTest extends AbstractJUnitTest {
    @Named("joc")
    @Inject
    JenkinsController jc;

    @Inject
    Jenkins j;

    @Test
    public void createSshKeys(){
        SshPrivateKeyCredential c = new SshPrivateKeyCredential(j);

        String username = "xyz";
        String privKey = "1212121122121212";
        c.create("GLOBAL", username, privKey);
        //now verify
        j.visit("credentials");
        Assert.assertEquals(j.find(j.by.input("_.username")).getAttribute("value"), "xyz");
    }
}
