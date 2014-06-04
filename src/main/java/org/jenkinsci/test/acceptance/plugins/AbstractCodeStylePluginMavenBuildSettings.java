package org.jenkinsci.test.acceptance.plugins;

import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Job;
import org.jenkinsci.test.acceptance.po.PageAreaImpl;

/**
 * Abstract job configuration class.
 * @author Fabian Trampusch
 */
public abstract class AbstractCodeStylePluginMavenBuildSettings extends PageAreaImpl {

    private boolean wasAdvancedClicked = false;
    private Control advanced = control("advanced-button");

    private Control canRunOnFailed = control("canRunOnFailed");

    private Control buildHealthyThreshold = control("healthy");
    private Control buildUnhealthyThreshold = control("unHealthy");

    private Control buildThresholdLimitHigh = control("thresholdLimit[high]");
    private Control buildThresholdLimitNormal = control("thresholdLimit[normal]");
    private Control buildThresholdLimitLow = control("thresholdLimit[low]");

    private Control buildUnstableTotalAll = control("unstableTotalAll");
    private Control buildUnstableTotalHigh = control("unstableTotalHigh");
    private Control buildUnstableTotalNormal = control("unstableTotalNormal");
    private Control buildUnstableTotalLow = control("unstableTotalLow");
    private Control buildFailedTotalAll = control("failedTotalAll");
    private Control buildFailedTotalHigh = control("failedTotalHigh");
    private Control buildFailedTotalNormal = control("failedTotalNormal");
    private Control buildFailedTotalLow = control("failedTotalLow");

    private Control canComputeNew = control("canComputeNew");

    /**
     * Constructor for the build settings page area.
     * @param parent the job currently being configured.
     * @param selectorPath the selector path used as prefix.
     */
    public AbstractCodeStylePluginMavenBuildSettings(Job parent, String selectorPath) {
        super(parent, "/hudson-plugins-" + selectorPath);
        this.advanced = control("advanced-button");
    }

    /**
     * Sets the threshold in percent after which a build is marked as stable.
     */
    public void setBuildHealthyThreshold(String threshold) {
        ensureAdvancedClicked();
        buildHealthyThreshold.set(threshold);
    }

    /**
     * Sets the threshold in percent after which a build is marked as stable.
     */
    public void setBuildUnhealthyThreshold(String threshold) {
        ensureAdvancedClicked();
        buildUnhealthyThreshold.set(threshold);
    }

    /**
     * Configures the job to use only "high" warnings for threshold.
     */
    public void setBuildThresholdLimitHigh() {
        ensureAdvancedClicked();
        buildThresholdLimitHigh.click();
    }

    /**
     * Configures the job to use "high" and "medium" warnings for threshold.
     */
    public void setBuildThresholdLimitNormal() {
        ensureAdvancedClicked();
        buildThresholdLimitNormal.click();
    }

    /**
     * Configures the job to use all warnings for threshold.
     */
    public void setBuildThresholdLimitLow() {
        ensureAdvancedClicked();
        buildThresholdLimitLow.click();
    }

    /**
     * Build is marked as unstable if at least these warnings are found.
     * @param threshold number of warnings to set the build to unstable.
     */
    public void setBuildUnstableTotalAll(String threshold) {
        ensureAdvancedClicked();
        buildUnstableTotalAll.set(threshold);
    }

    /**
     * BUild is marked as failed if at least tehse warnings are found.
     * @param threshold number of warnings to set the build to failed.
     */
    public void setBuildFailedTotalAll(String threshold) {
        ensureAdvancedClicked();
        buildFailedTotalAll.set(threshold);
    }

    /**
     * Ensures that advanced is clicked and the other controls are visible.
     */
    protected void ensureAdvancedClicked() {
        if (!wasAdvancedClicked) {
            advanced.click();
            wasAdvancedClicked = true;
        }
    }
}
