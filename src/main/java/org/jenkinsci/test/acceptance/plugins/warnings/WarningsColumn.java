package org.jenkinsci.test.acceptance.plugins.warnings;

import org.jenkinsci.test.acceptance.po.AbstractListViewColumn;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.ListView;

/**
 * View Column showing the number of compiler warnings.
 *
 * @author Ulli Hafner
 */
@Describable("Number of compiler warnings")
public class WarningsColumn extends AbstractListViewColumn {
    public WarningsColumn(final ListView parent, final String path) {
        super(parent, path);
    }
}
