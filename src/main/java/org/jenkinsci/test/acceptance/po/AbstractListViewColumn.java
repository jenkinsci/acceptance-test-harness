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
     * Deletes the column, i.e. removes it from the view.
     */
    public void delete() {
        control("repeatable-delete").click();
    }
}
