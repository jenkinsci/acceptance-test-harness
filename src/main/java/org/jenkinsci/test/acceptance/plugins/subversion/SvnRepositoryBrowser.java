package org.jenkinsci.test.acceptance.plugins.subversion;

import java.net.URL;
import org.jenkinsci.test.acceptance.po.PageObject;
import org.jenkinsci.test.acceptance.po.Scm;

/**
 * Superclass for the different repository browser accessable in the svn plugin
 *
 * @author Matthias Karl
 */
public class SvnRepositoryBrowser extends PageObject {

    public SvnRepositoryBrowser(Scm area, URL path) {
        super(area.getPage(), path);

    }


}
