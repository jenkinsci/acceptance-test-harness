package org.jenkinsci.test.acceptance.plugins.pmd;

import org.jenkinsci.test.acceptance.po.AbstractListViewColumn;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.ListView;

/**
 * A list view column showing the PMD warnings count.
 *
 * @author Fabian Trampusch
 */
@Describable("Number of PMD warnings")
public class PmdColumn extends AbstractListViewColumn {
    public PmdColumn(final ListView parent, final String path) {
        super(parent, path);
    }
}
