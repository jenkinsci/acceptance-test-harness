package org.jenkinsci.test.acceptance.plugins.subversion;

import java.net.URL;
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

    public Control url = control("/scm[1]/browser/url");

    public SvnRepositoryBrowserWebSvn(Scm area, URL path) {
        super(area, path);
    }
}
