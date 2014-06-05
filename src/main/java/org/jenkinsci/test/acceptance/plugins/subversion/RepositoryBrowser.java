package org.jenkinsci.test.acceptance.plugins.subversion;

import org.jenkinsci.test.acceptance.po.PageArea;
import org.jenkinsci.test.acceptance.po.Scm;

/**
 * Superclass for the different repositorybrowser accessable in the svn plugin
 *
 * @author Matthias Karl
 */
public class RepositoryBrowser extends PageArea {

    public RepositoryBrowser(Scm area, String path) {
        super(area.page, path);

    }


}
