package org.jenkinsci.test.acceptance.plugins.dashboard_view.controls;

import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.PageAreaImpl;
import org.jenkinsci.test.acceptance.po.PageObject;

/**
 * Provides the Area for configuring the basic settings for default portlets in the dashboard view.
 *
 * @author Peter Müller
 */
public class DashboardPortlets extends PageAreaImpl {

    private static final String PERCENTAGE_SYMBOL = "%";
    private static final String PIXEL_SYMBOL = "px";
    /**
     * Provides a checkbox to show the standard Jenkins list at the top of the page
     */
    private final Control includeStdJobList = control("/includeStdJobList");
    /**
     * Provides a checkbox to show the dashboard in full screen.
     */
    private final Control hideJenkinsPanels = control("/hideJenkinsPanels");
    /**
     * Provides a checkbox to enable setting of custom css parameters.
     */
    private final Control useCssStyle = control("/useCssStyle");
    /**
     * Provides the input for setting the width of the left portlet.
     */
    private final Control cssLeftPortletWidth = control("/useCssStyle/leftPortletWidth");
    /**
     * Provides the input for setting the width of the right portlet.
     */
    private final Control cssRightPortletWidth = control("/useCssStyle/rightPortletWidth");

    public DashboardPortlets(PageObject context, String path) {
        super(context, path);
    }

    /**
     * Enable the Std Job list in this View.
     *
     * @param state state, true if included
     */
    public void checkIncludeStdJobList(boolean state) {
        this.includeStdJobList.check(state);
    }

    /**
     * Hide the normal Jenkins panels.
     *
     * @param state state, true if hidden
     */
    public void checkHideJenkinsPanels(boolean state) {
        this.hideJenkinsPanels.check(state);
    }

    /**
     * Enable the additional setting of css styles.
     *
     * @param state state, true if enabled
     */
    private void useCssStyle(boolean state) {
        this.useCssStyle.check(state);
    }

    /**
     * Set the width of the left portlet in percent.
     *
     * @param percent percent %, the desired width of the left portlet
     */
    public void setLeftPortletWidthPercent(int percent) {
        setPortletWidthOnControl(cssLeftPortletWidth, percent, PERCENTAGE_SYMBOL);
    }

    /**
     * Set the width of the right portlet in percent.
     *
     * @param percent percent %, the desired width of the right portlet.
     */
    public void setRightPortletWidthPercent(int percent) {
        setPortletWidthOnControl(cssRightPortletWidth, percent, PERCENTAGE_SYMBOL);
    }

    /**
     * Set the width of the left portlet in pixels.
     *
     * @param pixel pixel px, the width in pixel unit
     */
    public void setLeftPortletWidthPixel(int pixel) {
        setPortletWidthOnControl(cssLeftPortletWidth, pixel, PIXEL_SYMBOL);
    }

    /**
     * Set the width of the right portlet in pixels.
     *
     * @param pixel pixel px, the width in pixel unit
     */
    public void setRightPortletWidthPixel(int pixel) {
        setPortletWidthOnControl(cssRightPortletWidth, pixel, PIXEL_SYMBOL);
    }

    private void setPortletWidthOnControl(Control cssPortletWidth, int width, String symbol) {
        if (width < 0) {
            throw new IllegalArgumentException("Width can't be negative!");
        }
        useCssStyle(true);
        cssPortletWidth.set(width + symbol);
    }
}
