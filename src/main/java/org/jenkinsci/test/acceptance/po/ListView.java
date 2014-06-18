package org.jenkinsci.test.acceptance.po;

import com.google.inject.Injector;

import java.net.URL;

/**
 * @author Kohsuke Kawaguchi
 */
@Describable("List View")
public class ListView extends View {

    public ListView(Injector injector, URL url) {
        super(injector, url);
    }

    /**
     * Adds a column on the dashboard.
     * @param type The class object of the type of the new dashboard column.
     * @param <T> The type of the new dashboard column.
     * @return The new dashboard column.
     */
    public <T extends ListViewColumn> T addColumn(Class<T> type) {
        Control addColumns = control("/hetero-list-add[columns]");
        addColumns.selectDropdownMenu(type);
        String path = last(by.css("[name='columns']")).getAttribute("path");
        return newInstance(type, this, path);
    }

    /**
     * Configures the listview to include all jobs.
     */
    public void matchAllJobs() {
        control("/useincluderegex").check();
        String matchEverything = ".*";
        Control regexJobFilter = control("/useincluderegex/includeRegex");
        regexJobFilter.set(matchEverything);
    }

    /**
     * Deletes the listview.
     */
    public void delete() {
        configure();
        clickLink("Delete View");
        clickButton("Yes");
    }
}
