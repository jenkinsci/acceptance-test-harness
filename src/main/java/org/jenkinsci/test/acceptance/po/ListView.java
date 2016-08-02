package org.jenkinsci.test.acceptance.po;

import com.google.inject.Injector;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * @author Kohsuke Kawaguchi
 */
@Describable("List View")
public class ListView extends View {

    private List<ListViewColumn> columns = new ArrayList<>();

    public ListView(Injector injector, URL url) {
        super(injector, url);
    }

    /**
     * Adds a column on the dashboard.
     *
     * @param type The class object of the type of the new dashboard column.
     * @param <T>  The type of the new dashboard column.
     * @return The new dashboard column.
     */
    public <T extends ListViewColumn> T addColumn(final Class<T> type) {
        String path = createPageArea("columns", new Runnable() {
            @Override public void run() {
                control("/hetero-list-add[columns]").selectDropdownMenu(type);
            }
        });
        T col = newInstance(type, this, path);
        columns.add(col);
        return col;
    }

    /**
     * @return the column to the corresponding type
     */
    public <T extends ListViewColumn> T getColumn(Class<T> type) {
        for (ListViewColumn p : columns) {
            if (type.isAssignableFrom(p.getClass()))
                return (T) p;
        }
        throw new NoSuchElementException();
    }

    /**
     * Explicitly add a job to the view.
     */
    public void addJob(Job job) {
        ensureConfigPage();
        check(job.name);
    }
}
