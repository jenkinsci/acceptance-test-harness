package org.jenkinsci.test.acceptance.plugins.dashboard_view.controls;

import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.PageAreaImpl;
import org.jenkinsci.test.acceptance.po.PageObject;

/**
 * Created by peter-mueller on 04.05.17.
 */
public class DashboardPortlets extends PageAreaImpl {

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
        if (percent < 0) {
            throw new IllegalArgumentException("Width can't be negative!");
        }
        useCssStyle(true);
        this.cssLeftPortletWidth.set(percent + "%");
    }

    /**
     * Set the width of the right portlet in percent.
     *
     * @param percent percent %, the desired width of the right portlet.
     */
    public void setRightPortletWidthPercent(int percent) {
        if (percent < 0) {
            throw new IllegalArgumentException("Width can't be negative!");
        }
        useCssStyle(true);
        this.cssRightPortletWidth.set(percent + "%");
    }

    /**
     * Set the width of the left portlet in pixels.
     *
     * @param pixel pixel px, the width in pixel unit
     */
    public void setLeftPortletWidthPixel(int pixel) {
        if (pixel < 0) {
            throw new IllegalArgumentException("Width can't be negative!");
        }
        useCssStyle(true);
        this.cssLeftPortletWidth.set(pixel + "px");
    }

    /**
     * Set the
     *
     * @param pixel
     */
    public void setRightPortletWidthPixel(int pixel) {
        if (pixel < 0) {
            throw new IllegalArgumentException("Width can't be negative!");
        }
        useCssStyle(true);
        this.cssRightPortletWidth.set(pixel + "px");
    }
}
