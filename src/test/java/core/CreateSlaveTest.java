package core;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.plugins.ssh_credentials.SshCredentialDialog;
import org.jenkinsci.test.acceptance.plugins.ssh_slaves.SshSlaveLauncher;
import org.jenkinsci.test.acceptance.po.DumbSlave;
import org.jenkinsci.test.acceptance.po.SshPrivateKeyCredential;
import org.junit.Test;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

/**
 * @author Vivek Pandey
 */
public class CreateSlaveTest extends AbstractJUnitTest {
    @Test
    public void newSlave(){

        // Just to make sure the dumb slave is set up properly, we should seed it
        // with a FS root and executors
        final DumbSlave s = jenkins.slaves.create(DumbSlave.class);

        SshSlaveLauncher l = s.setLauncher(SshSlaveLauncher.class);

        String username = "user1";
        String privateKey = "1212122112";
        String description = "Ssh key";

        l.host.set("127.0.0.1");
        l.credentialsId.resolve();  // make sure this exists

        try{
            l.credentialsId.select(String.format("%s (%s)", username, description));
        }catch (NoSuchElementException e){
            //ignore
        }

        SshCredentialDialog f = l.addCredential();

        f.kind.select("SSH Username with private key");
        f.description.set(description);
        f.username.set(username);
        f.selectEnterDirectly().privateKey.set(privateKey);
        f.add();

        l.credentialsId.select(String.format("%s (%s)", username, description));

        clickButton("Save");
    }

    @Test
    public void newSlaveWithExistingCredential(){

        SshPrivateKeyCredential c = new SshPrivateKeyCredential(jenkins);

        String username = "xyz";
        String description = "SSH Key setup";
        String privateKey = "1212121122121212";
        c.create("GLOBAL", username, privateKey);

        //now verify
        jenkins.visit("credentials");
        assertEquals(jenkins.find(by.input("_.username")).getAttribute("value"), username);
        assertEquals(jenkins.find(by.input("_.privateKey")).getText(), privateKey);

        // Just to make sure the dumb slave is set up properly, we should seed it
        // with a FS root and executors
        final DumbSlave s = jenkins.slaves.create(DumbSlave.class);

        find(by.input("_.host")).sendKeys("127.0.0.1");

        WebElement credentialSelect = s.find(by.input("_.credentialsId"));
        assertNotNull(credentialSelect);

        WebElement keyItem = credentialSelect.findElement(by.option(String.format("%s (%s)", username, description)));
        assertNotNull(keyItem);

        clickButton("Save");
    }

}
