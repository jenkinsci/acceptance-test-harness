package org.jenkinsci.test.acceptance.plugins.ssh_credentials;

import java.time.Duration;
import org.jenkinsci.test.acceptance.plugins.credentials.BaseStandardCredentials;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.PageAreaImpl;
import org.jenkinsci.test.acceptance.po.PageObject;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.WebElement;

/**
 * @author Kohsuke Kawaguchi
 */
@Describable("SSH Username with private key")
public class SshPrivateKeyCredential extends BaseStandardCredentials {
    public final Control username = control(by.name("_.username"));
    public final Control passphrase = control(by.name("_.passphrase"));

    public SshPrivateKeyCredential(PageObject context, String path) {
        super(context, path);
    }

    public SshPrivateKeyCredential(PageAreaImpl area, String relativePath) {
        super(area, relativePath);
    }

    public SshPrivateKeyCredential enterDirectly(String privateKey) {
        selectEnterDirectly().privateKey.set(privateKey);
        return this;
    }

    public Direct selectEnterDirectly() {
        WebElement e = choose("Enter directly");
        waitFor()
                .withTimeout(Duration.ofSeconds(1))
                .pollingEvery(Duration.ofMillis(100))
                .ignoring(ElementNotInteractableException.class)
                .until(() -> {
                    WebElement button = getElement(By.className("secret-update-btn"));
                    button.click();
                    return true;
                });

        return new Direct(getPage(), e.getAttribute("path"));
    }

    public static class Direct extends PageAreaImpl {
        public final Control privateKey = control(by.name("_.privateKey"));

        public Direct(PageObject parent, String path) {
            super(parent, path);
        }
    }
}
