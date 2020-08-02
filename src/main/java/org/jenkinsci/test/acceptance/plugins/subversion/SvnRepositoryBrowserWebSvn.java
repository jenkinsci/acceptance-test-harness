package org.jenkinsci.test.acceptance.plugins.subversion;

import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.Scm;

import java.net.URL;

/**
 * PageArea for the WebSvn repository browser
 *
 * @author Matthias Karl
 */
@Describable("WebSVN")
public class SvnRepositoryBrowserWebSvn extends SvnRepositoryBrowser {

    public Control url = control(by.xpath("//td[@class='setting-name' and text()='%s']/../td[@class='setting-main']/input | //div[contains(@class, 'setting-name') and text()='%s']/../div[@class='setting-main']/input", "URL", "URL"));


    public SvnRepositoryBrowserWebSvn(Scm area, URL path) {
        super(area, path);
    }
}
