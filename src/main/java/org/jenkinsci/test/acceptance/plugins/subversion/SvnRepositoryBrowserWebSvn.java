package org.jenkinsci.test.acceptance.plugins.subversion;

import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.Scm;

/**
 * PageArea for the WebSvn repository browser
 *
 * @author Matthias Karl
 */
@Describable("WebSVN")
public class SvnRepositoryBrowserWebSvn extends SvnRepositoryBrowser {

    public Control url = control(by.xpath("//td[@class='setting-name' and text()='%s']/../td[@class='setting-main']/input", "URL"));


    public SvnRepositoryBrowserWebSvn(Scm area, String path) {
        super(area, path);
    }
}
