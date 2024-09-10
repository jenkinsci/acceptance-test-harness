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
public class JobFiltersArea extends PageAreaImpl {
    /**
     * Button to add a new status filter
     */
    private final Control addStatusFilter = control("/hetero-list-add[jobFilters]");
    /**
     * Dropdown to select the type of {@link StatusFilter}.
     */
    private final Control statusFilter = control("/jobFilters/statusFilter");
    /**
     * Checkbox to enable recursion in subfolders.
     */
    private final Control recurseInSubfolders = control("/recurse");
    /**
     * Checkbox to use a regular expression to include jobs into the view.
     */
    private final Control useIncludeRegex = control("/useincluderegex");
    /**
     * Text input for the regex, only visible if useIncludeRegex is checked.
     */
    private final Control includeRegex = control("/useincluderegex/includeRegex");

    /**
     * Create a new Area for the configuration regarding the job filters.
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
        this.addStatusFilter.selectDropdownMenu("Status Filter");
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
     * Set a regex to include jobs only if they match the expression
     *
     * @param regex regex the regex to filter the job names
     */
    public void setIncludeRegex(String regex) {
        this.useIncludeRegex.check(true);
        includeRegex.set(regex);
    }

    /**
     * Provides a collection of all possible filter types.
     *
     * @author peter-mueller
     */
    public enum StatusFilter {
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
