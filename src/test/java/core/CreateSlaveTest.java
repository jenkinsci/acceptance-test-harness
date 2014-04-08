package core;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.Since;
import org.jenkinsci.test.acceptance.plugins.credentials.ManagedCredentials;
import org.jenkinsci.test.acceptance.plugins.ssh_credentials.SshCredentialDialog;
import org.jenkinsci.test.acceptance.plugins.ssh_credentials.SshPrivateKeyCredential;
import org.jenkinsci.test.acceptance.plugins.ssh_slaves.SshSlaveLauncher;
import org.jenkinsci.test.acceptance.po.DumbSlave;
import org.junit.Test;
import org.openqa.selenium.NoSuchElementException;

/**
 * @author Vivek Pandey
 */
public class CreateSlaveTest extends AbstractJUnitTest {
    @Test @Since("1.544")
    public void newSlave() {
        // this test requires a newer version of credentials plugin that has inline "Add" button
        // I'm not sure exactly which version it is, but 1.532 LTS doesn't have it, and 1.555 has it,
        // so it's somewhere in between
        // TODO: this should be converted to "@WithPlugin("ssh-credentials") with specific version tag,
        // not the core version

        // Just to make sure the dumb slave is set up properly, we should seed it
        // with a FS root and executors
        final DumbSlave s = jenkins.slaves.create(DumbSlave.class);
        {
            SshSlaveLauncher l = s.setLauncher(SshSlaveLauncher.class);

            String username = "user1";
            String privateKey = "1212122112";
            String description = "Ssh key";

            l.host.set("127.0.0.1");
            l.credentialsId.resolve();  // make sure this exists

            try {
                l.credentialsId.select(String.format("%s (%s)", username, description));
                fail();
            } catch (NoSuchElementException e) {
                //ignore
            }

            SshCredentialDialog f = l.addCredential();
            {
                SshPrivateKeyCredential sc = f.select(SshPrivateKeyCredential.class);
                sc.description.set(description);
                sc.username.set(username);
                sc.selectEnterDirectly().privateKey.set(privateKey);
            }
            f.add();

            l.credentialsId.select(String.format("%s (%s)", username, description));
        }
        s.save();
    }

    @Test
    public void newSlaveWithExistingCredential(){

        String username = "xyz";
        String description = "SSH Key setup";
        String privateKey = "1212121122121212";

        ManagedCredentials c = new ManagedCredentials(jenkins);
        c.open();
        {
            SshPrivateKeyCredential sc = c.add(SshPrivateKeyCredential.class);
            sc.username.set(username);
            sc.description.set(description);
            sc.selectEnterDirectly().privateKey.set(privateKey);
        }
        c.save();

        //now verify
        c.open();
        assertEquals(find(by.input("_.username")).getAttribute("value"), username);
        assertEquals(find(by.input("_.privateKey")).getText(), privateKey);

        // Just to make sure the dumb slave is set up properly, we should seed it
        // with a FS root and executors
        final DumbSlave s = jenkins.slaves.create(DumbSlave.class);
        SshSlaveLauncher l = s.setLauncher(SshSlaveLauncher.class);
        l.host.set("127.0.0.1");

        l.credentialsId.select(String.format("%s (%s)", username, description));
    }

}
