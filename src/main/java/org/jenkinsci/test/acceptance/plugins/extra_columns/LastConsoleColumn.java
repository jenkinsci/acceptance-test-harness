package org.jenkinsci.test.acceptance.plugins.extra_columns;

import org.jenkinsci.test.acceptance.po.AbstractListViewColumn;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.ListView;

/**
 * Column for the last console output.
 *
 * @author Ullrich Hafner
 */
@Describable("Last/Current Build Console Output")
public class LastConsoleColumn extends AbstractListViewColumn {
    public LastConsoleColumn(ListView parent, String path) {
        super(parent, path);
    }
}
