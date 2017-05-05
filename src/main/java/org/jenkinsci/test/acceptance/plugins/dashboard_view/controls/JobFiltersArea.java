package org.jenkinsci.test.acceptance.plugins.dashboard_view.controls;

import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.PageAreaImpl;
import org.jenkinsci.test.acceptance.po.PageObject;

/**
 * Provides a small area of the Dashboard Configuration that can limit the Jenkins Jobs
 * displayed in the Dashboard.
 *
 * @author peter-mueller
 */
public class JobFiltersArea extends PageAreaImpl {
    /**
     * Dropdown to select the type of {@link StatusFilter}.
     */
    private final Control statusFilter = control("/statusFilter");
    /**
     * Checkbox to enable recursion in subfolders.
     */
    private final Control recurseInSubfolders = control("/recurse");
    /**
     * Checkbox to use a regular expression to include jobs into the view.
     */
    private final Control useIncludeRegex = control("/useincluderegex");

    /**
     * Create a new Area for the configuration regarding the job filters.
     *
     * @param context
     * @param path
     */
    public JobFiltersArea(PageObject context, String path) {
        super(context, path);
    }

    /**
     * Select the filter type in the dropdown.
     *
     * @param statusFilter the filter type to use
     */
    public void setStatusFilter(StatusFilter statusFilter) {
        this.statusFilter.select(statusFilter.getCaption());

    }

    /**
     * Change the state of the checkbox for the option of recursion in subfolders.
     *
     * @param state true if enabled
     */
    public void setRecurseInSubfolders(boolean state) {
        this.recurseInSubfolders.check(state);
    }

    /**
     * Change the state of the checkbox for to option to include a job by regex.
     *
     * @param state true if enabled
     */
    public void setUseIncludeRegex(boolean state) {
        this.useIncludeRegex.check(state);
    }


    /**
     * Provides a collection of all possible filter types.
     *
     * @author peter-mueller
     */
    public static enum StatusFilter {
        /**
         * Present all jobs in the dashboard.
         */
        ALL("All selected jobs"),
        /**
         * Only show enabled jobs.
         */
        ENABLED("Enabled jobs only"),
        /**
         * Obly show disabled jobs.
         */
        DISABLED("Disabled jobs only");

        /**
         * The caption text in the dropdown.
         */
        private final String caption;

        /**
         * Create a Filter type with the caption of the dropdown.
         *
         * @param caption the text of the caption
         */
        StatusFilter(String caption) {
            this.caption = caption;
        }

        /**
         * Get the caption of this filter option.
         *
         * @return the caption text
         */
        public String getCaption() {
            return caption;
        }
    }


}
