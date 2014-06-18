package org.jenkinsci.test.acceptance.po;

/**
 * Abstract base class for configuring dash board columns.
 *
 * @author Fabian Trampusch
 */
public class AbstractListViewColumn extends PageAreaImpl implements ListViewColumn {

    protected AbstractListViewColumn(ListView parent, String path) {
        super(parent, path);
    }

    /**
     * Presses "delete" on the column.
     */
    public void delete() {
        control("repeatable-delete").click();
    }

}
