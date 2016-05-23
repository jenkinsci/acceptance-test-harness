package org.jenkinsci.test.acceptance.plugins.credentialsbinding;

import org.jenkinsci.test.acceptance.po.ContainerPageObject;
import org.jenkinsci.test.acceptance.po.Describable;

@Describable("Secret text")
public class SecretStringCredentialsBinding extends CredentialsBinding {

    public SecretStringCredentialsBinding(ContainerPageObject po, String path) {
        super(po, path);
    }
}
