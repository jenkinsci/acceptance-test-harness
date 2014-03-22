package core;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.po.DumbSlave;
import org.jenkinsci.test.acceptance.po.SshPrivateKeyCredential;
import org.junit.Assert;
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

        String username = "user1";
        String privateKey = "1212122112";
        String description = "Ssh key";

        find(by.input("_.host")).sendKeys("127.0.0.1");

        WebElement credentialSelect = find(by.input("_.credentialsId"));
        assertNotNull(credentialSelect);

        WebElement keyItem=null;
        try{
            keyItem = credentialSelect.findElement(by.option(String.format("%s (%s)", username, description)));
        }catch (NoSuchElementException e){
            //ignore
        }
        assertNull(keyItem);

        clickButton("Add");

        WebElement select = find(by.xpath(".//form[@id='credentials-dialog-form']//select[@class='setting-input dropdownList']"))
                .findElement(by.option("SSH Username with private key"));
        select.click();
        find(by.input("_.description")).sendKeys(description);
        find(by.input("_.username")).clear(); //it's always pre-filled with system default user
        find(by.input("_.username")).sendKeys(username);
        find(by.input("_.privateKey")).sendKeys(privateKey);
        find(by.xpath("//button[@id='credentials-add-submit-button']")).click();


        keyItem = credentialSelect.findElement(by.option(String.format("%s (%s)", username, description)));
        assertNotNull(keyItem);

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
