package org.jenkinsci.test.acceptance.plugins.ssh_slaves;

import org.jenkinsci.test.acceptance.plugins.ssh_credentials.SshCredentialDialog;
import org.jenkinsci.test.acceptance.plugins.ssh_slaves.SshSlaveLauncher;
import org.jenkinsci.test.acceptance.po.PageObject;
import org.openqa.selenium.WebElement;

/**
 * Created by raul on 15/06/16.
 */
public class SshSlaveLauncher2 extends SshSlaveLauncher {
    public SshSlaveLauncher2(PageObject context, String path) {
        super(context, path);
    }

    @Override
    public SshCredentialDialog addCredential() {
        self().findElement(by.button("Add")).click();
        elasticSleep(500);
        control(by.xpath("//*[@id=\"yui-gen2\"]")).click();
        return new SshCredentialDialog(getPage(), "/credentials");
    }

}
