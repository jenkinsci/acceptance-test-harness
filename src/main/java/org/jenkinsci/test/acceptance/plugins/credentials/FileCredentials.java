package org.jenkinsci.test.acceptance.plugins.credentials;

import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.PageAreaImpl;
import org.jenkinsci.test.acceptance.po.PageObject;

@Describable("Secret file")
public class FileCredentials extends BaseStandardCredentials {

    public Control file = control("file");

    public FileCredentials(PageObject context, String path) {
        super(context, path);
    }

    public FileCredentials(PageAreaImpl area, String relativePath) {
        super(area, relativePath);
    }
}
