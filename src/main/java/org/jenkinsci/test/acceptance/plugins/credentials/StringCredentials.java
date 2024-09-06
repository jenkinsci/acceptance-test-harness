package org.jenkinsci.test.acceptance.plugins.credentials;

import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.PageAreaImpl;
import org.jenkinsci.test.acceptance.po.PageObject;

@Describable("Secret text")
public class StringCredentials extends BaseStandardCredentials {

    public Control secret = control("secret");

    public StringCredentials(PageObject context, String path) {
        super(context, path);
    }

    public StringCredentials(PageAreaImpl area, String relativePath) {
        super(area, relativePath);
    }
}
