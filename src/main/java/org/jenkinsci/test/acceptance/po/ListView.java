package org.jenkinsci.test.acceptance.po;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import com.google.inject.Injector;

/**
 * @author Kohsuke Kawaguchi
 */
@Describable("List View")
public class ListView extends View {
    /**
     * Creates a new list view with the specified column.
     *
     * @param owner       owner of the list view (jenkins, folder, etc.)
     * @param columnClass the class of the column to add
     * @param <T>         the type of the column
     * @return the created column
     */
    public static <T extends ListViewColumn> T createWithColumn(final Container owner, final Class<T> columnClass) {
        ListView view = owner.getViews().create(ListView.class, createRandomName());
        view.configure();
        view.matchAllJobs();
        T column = view.addColumn(columnClass);
        view.save();
        return column;
    }

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
        String path = createPageArea("/columns", new Runnable() {
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

    public void scheduleJob(String name) {
        open();
        find(by.xpath("//a[contains(@href, '/%1$s/build?')]/img[contains(@title, 'Schedule a')]", name)).click();
    }

    public void scheduleJob(String name, Map<String, Object> params) {
        scheduleJob(name);
    }
}
