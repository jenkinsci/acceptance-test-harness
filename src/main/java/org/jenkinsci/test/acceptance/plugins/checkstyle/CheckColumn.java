package org.jenkinsci.test.acceptance.plugins.checkstyle;

import org.jenkinsci.test.acceptance.po.AbstractListViewColumn;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.ListView;

/**
 * A list view column showing the CheckStyle warnings count.
 *
 * @author Fabian Trampusch
 */
@Describable("Number of Checkstyle warnings")
public class CheckColumn extends AbstractListViewColumn {
    public CheckColumn(final ListView parent, final String path) {
        super(parent, path);
    }
}
