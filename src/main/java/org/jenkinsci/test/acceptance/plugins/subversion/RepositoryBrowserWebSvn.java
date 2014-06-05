package org.jenkinsci.test.acceptance.plugins.subversion;

import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.Scm;

/**
 * PO for the WebSvn repository browser
 *
 * @author Matthias Karl
 */
@Describable("WebSVN")
public class RepositoryBrowserWebSvn extends RepositoryBrowser {

    public Control url = control(by.xpath("//td[@class='setting-name' and text()='%s']/../td[@class='setting-main']/input", "URL"));


    public RepositoryBrowserWebSvn(Scm area, String path) {
        super(area, path);
    }
}
