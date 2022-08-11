package org.jenkinsci.test.acceptance.plugins.dashboard_view.controls;

import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.PageAreaImpl;
import org.jenkinsci.test.acceptance.po.PageObject;

/**
 * Provides a small area of the Dashboard Configuration that can limit the Jenkins Jobs
 * displayed in the Dashboard.
 *
 * @author Peter Müller
 */
public class MainArea extends PageAreaImpl {

    /**
     * Name Input of the Dashboard
     */
    private final Control name = control("/name");
    /**
     * Description Input
     */
    private final Control description = control("/description");
    /**
     * Filter Build Queue Checkbox
     */
    private final Control filterBuildQueue = control("/filterQueue");
    /**
     * Filter Build Executors Checkbox
     */
    private final Control filterBuildExecutors = control("/filterExecutors");

    /**
     * Create a new Area for the configuration of title and descriptions.
     */
    public MainArea(PageObject context, String path) {
        super(context, path);
    }

    /**
     * Set the name of the dashboard.
     *
     * @param name the name of the table
     */
    public void setName(String name) {
        this.name.set(name);
    }

    /**
     * Set the description of the table.
     *
     * @param description the description text
     */
    public void setDescription(String description) {
        this.description.set(description);
    }

    /**
     * Show only jobs of this view in the queue.
     *
     * @param state if checked, only jobs in this view will be shown in the queue.
     */
    public void setFilterBuildQueue(boolean state) {
        this.filterBuildQueue.check(state);
    }

    /**
     * Check Filter Build Executors
     *
     * @param state if checked, only those build executors will be shown that could execute the jobs in this view.
     */
    public void setFilterBuildExecutors(boolean state) {
        this.filterBuildExecutors.check(state);
    }
}
