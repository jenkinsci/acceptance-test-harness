package org.jenkinsci.test.acceptance.plugins.credentialsbinding;

import org.jenkinsci.test.acceptance.po.ContainerPageObject;
import org.jenkinsci.test.acceptance.po.Describable;

@Describable("Secret file")
public class SecretFileCredentialsBinding extends CredentialsBinding {

    public SecretFileCredentialsBinding(ContainerPageObject po, String path) {
        super(po, path);
    }
}
