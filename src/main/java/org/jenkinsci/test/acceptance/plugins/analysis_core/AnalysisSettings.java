package org.jenkinsci.test.acceptance.plugins.analysis_core;

import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Job;
import org.jenkinsci.test.acceptance.po.PageAreaImpl;
import org.jenkinsci.test.acceptance.po.PostBuildStep;

/**
 * Abstract job configuration class.
 * @author Fabian Trampusch
 */
public abstract class AnalysisSettings extends PageAreaImpl implements PostBuildStep {

    protected Control advanced = control("advanced-button");

    protected Control canRunOnFailed = control("canRunOnFailed");

    protected Control buildHealthyThreshold = control("healthy");
    protected Control buildUnhealthyThreshold = control("unHealthy");

    protected Control buildThresholdLimitHigh = control("thresholdLimit[high]");
    protected Control buildThresholdLimitNormal = control("thresholdLimit[normal]");
    protected Control buildThresholdLimitLow = control("thresholdLimit[low]");

    protected Control buildUnstableTotalAll = control("unstableTotalAll");
    protected Control buildUnstableTotalHigh = control("unstableTotalHigh");
    protected Control buildUnstableTotalNormal = control("unstableTotalNormal");
    protected Control buildUnstableTotalLow = control("unstableTotalLow");
    protected Control buildFailedTotalAll = control("failedTotalAll");
    protected Control buildFailedTotalHigh = control("failedTotalHigh");
    protected Control buildFailedTotalNormal = control("failedTotalNormal");
    protected Control buildFailedTotalLow = control("failedTotalLow");

    protected Control canComputeNew = control("canComputeNew");
    protected Control newWarningsThresholdFailed = control("canComputeNew/failedNewAll");
    protected Control newWarningsThresholdUnstable = control("canComputeNew/unstableNewAll");
    protected Control useDeltaValues = control("canComputeNew/useDeltaValues");

    /**
     * Constructor for the build settings page area.
     * @param parent the job currently being configured.
     * @param selectorPath the selector path used as prefix.
     */
    public AnalysisSettings(Job parent, String selectorPath) {
        super(parent, selectorPath);
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
     * Build is marked as unstable if at least these warnings of high
     * priority are found.
     * @param threshold number of warnings to set the build to unstable.
     */
    public void setBuildUnstableTotalHigh(String threshold) {
        ensureAdvancedClicked();
        buildUnstableTotalHigh.set(threshold);
    }

    /**
     * Build is marked as unstable if at least these warnings of normal
     * priority are found.
     * @param threshold number of warnings to set the build to unstable.
     */
    public void setBuildUnstableTotalNormal(String threshold) {
        ensureAdvancedClicked();
        buildUnstableTotalNormal.set(threshold);
    }

    /**
     * Build is marked as unstable if at least these warnings of low
     * priority are found.
     * @param threshold number of warnings to set the build to unstable.
     */
    public void setBuildUnstableTotalLow(String threshold) {
        ensureAdvancedClicked();
        buildUnstableTotalLow.set(threshold);
    }

    /**
     * Build is marked as failed if at least these warnings are found.
     * @param threshold number of warnings to set the build to failed.
     */
    public void setBuildFailedTotalAll(String threshold) {
        ensureAdvancedClicked();
        buildFailedTotalAll.set(threshold);
    }

    /**
     * Build is marked as failed if at least these warnings of high
     * priority are found.
     * @param threshold number of warnings to set the build to unstable.
     */
    public void setBuildFailedTotalHigh(String threshold) {
        ensureAdvancedClicked();
        buildFailedTotalHigh.set(threshold);
    }

    /**
     * Build is marked as failed if at least these warnings of normal
     * priority are found.
     * @param threshold number of warnings to set the build to unstable.
     */
    public void setBuildFailedTotalNormal(String threshold) {
        ensureAdvancedClicked();
        buildFailedTotalNormal.set(threshold);
    }

    /**
     * Build is marked as failed if at least these warnings of low
     * priority are found.
     * @param threshold number of warnings to set the build to unstable.
     */
    public void setBuildFailedTotalLow(String threshold) {
        ensureAdvancedClicked();
        buildFailedTotalLow.set(threshold);
    }

    /**
     * Build is marked as failed if at least these new warnings are found.
     * @param threshold number of new warnings to set the build to failed.
     */
    public void setNewWarningsThresholdFailed(String threshold) {
        ensureAdvancedClicked();
        canComputeNew.check(true);
        newWarningsThresholdFailed.set(threshold);
    }

    /**
     * Build is marked as unstable if at least these new warnings are found.
     * @param threshold number of new warnings to set the build to unstable.
     */
    public void setNewWarningsThresholdUnstable(String threshold) {
        ensureAdvancedClicked();
        canComputeNew.check(true);
        newWarningsThresholdUnstable.set(threshold);
    }

    /**
     * Detect new warnings in comparison with the last build.
     * @param deltaValues compare if true
     */
    public void setUseDeltaValues(boolean deltaValues) {
        ensureAdvancedClicked();
        canComputeNew.check(true);
        useDeltaValues.check(deltaValues);
    }

    /**
     * Decides if the code analyzer shall run if the build has failed.
     * @param canRun build if true.
     */
    public void setCanRunOnFailed(boolean canRun) {
        ensureAdvancedClicked();
        canRunOnFailed.check(canRun);
    }

    /**
     * Ensures that advanced is clicked and the other controls are visible.
     */
    protected void ensureAdvancedClicked() {
        if (advanced.exists()) {
            advanced.click();
        }
    }
}
