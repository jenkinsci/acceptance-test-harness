package org.jenkinsci.test.acceptance.plugins.findbugs;

import org.jenkinsci.test.acceptance.po.AbstractListViewColumn;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.ListView;

/**
 * A list view column showing the FindBugs warnings count.
 *
 * @author Fabian Trampusch
 */
@Describable("Number of FindBugs warnings")
public class FindBugsColumn extends AbstractListViewColumn {
    public FindBugsColumn(final ListView parent, final String path) {
        super(parent, path);
    }
}
