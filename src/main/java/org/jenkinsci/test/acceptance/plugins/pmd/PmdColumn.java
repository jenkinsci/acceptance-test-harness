package org.jenkinsci.test.acceptance.plugins.pmd;

import org.jenkinsci.test.acceptance.po.AbstractListViewColumn;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.ListView;

/**
 * The PMD Column.
 *
 * @author Fabian Trampusch
 */
@Describable("Number of PMD warnings")
public class PmdColumn extends AbstractListViewColumn {
    public PmdColumn(ListView parent, String path) {
        super(parent, path);
    }
}
