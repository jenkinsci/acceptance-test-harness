package org.jenkinsci.test.acceptance.plugins.checkstyle;

import org.jenkinsci.test.acceptance.po.AbstractListViewColumn;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.ListView;

/**
 * The Checkstyle Column.
 *
 * @author Fabian Trampusch
 */
@Describable("Number of Checkstyle warnings")
public class CheckstyleColumn extends AbstractListViewColumn {
    public CheckstyleColumn(ListView parent, String path) {
        super(parent, path);
    }
}
