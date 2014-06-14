package org.jenkinsci.test.acceptance.plugins.findbugs;

import org.jenkinsci.test.acceptance.po.AbstractListViewColumn;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.ListView;

/**
 * The Findbugs Column.
 *
 * @author Fabian Trampusch
 */
@Describable("Number of FindBugs warnings")
public class FindbugsColumn extends AbstractListViewColumn {
    public FindbugsColumn(ListView parent, String path) {
        super(parent, path);
    }
}
