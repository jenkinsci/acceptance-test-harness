package org.jenkinsci.test.acceptance.plugins.tasks;

import org.jenkinsci.test.acceptance.po.AbstractListViewColumn;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.ListView;

/**
 * A list view column showing the number of open tasks.
 *
 * @author Ullrich Hafner
 */
@Describable("Number of open tasks")
public class TasksColumn extends AbstractListViewColumn {
    public TasksColumn(final ListView parent, final String path) {
        super(parent, path);
    }
}
