package org.jenkinsci.test.acceptance.plugins.analysis_core;

import java.util.List;

import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Job;
import org.jenkinsci.test.acceptance.po.PageAreaImpl;
import org.jenkinsci.test.acceptance.po.PostBuildStep;
import org.openqa.selenium.WebElement;

/**
 * Job settings for all static analysis plug-ins.
 *
 * @author Fabian Trampusch
 */
public abstract class AnalysisSettings extends PageAreaImpl implements PostBuildStep {
    protected Control advanced = control("advanced-button");

    protected Control canRunOnFailed = control("canRunOnFailed");
    protected Control canResolveRelativePaths = control("canResolveRelativePaths");

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
    protected Control newWarningsThresholdFailed = control("failedNewAll", "canComputeNew/failedNewAll");
    protected Control newWarningsThresholdUnstable = control("unstableNewAll", "canComputeNew/unstableNewAll");
    protected Control useDeltaValues = control("useDeltaValues", "canComputeNew/useDeltaValues");
    protected Control usePreviousBuild = control("usePreviousBuildAsReference", "canComputeNew/usePreviousBuildAsReference");

    /**
     * Creates a new instance of {@link AnalysisSettings}.
     *
     * @param parent       the job currently being configured.
     * @param selectorPath the selector path used as prefix.
     */
    public AnalysisSettings(Job parent, String selectorPath) {
        super(parent, selectorPath);

        advanced = control("advanced-button");
    }

    /**
     * Sets the threshold in percent after which a build is marked as stable.
     *
     * @param threshold number of warnings threshold to get a healthy build
     */
    public void setBuildHealthyThreshold(final int threshold) {
        ensureAdvancedClicked();
        buildHealthyThreshold.set(threshold);
    }

    /**
     * Sets the threshold in percent after which a build is marked as stable.
     *
     * @param threshold number of warnings threshold to get an unhealthy build
     */
    public void setBuildUnhealthyThreshold(final int threshold) {
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
     *
     * @param threshold number of warnings to set the build to unstable.
     */
    public void setBuildUnstableTotalAll(String threshold) {
        ensureAdvancedClicked();
        buildUnstableTotalAll.set(threshold);
    }

    /**
     * Build is marked as unstable if at least these warnings of high priority are found.
     *
     * @param threshold number of warnings to set the build to unstable.
     */
    public void setBuildUnstableTotalHigh(String threshold) {
        ensureAdvancedClicked();
        buildUnstableTotalHigh.set(threshold);
    }

    /**
     * Build is marked as unstable if at least these warnings of normal priority are found.
     *
     * @param threshold number of warnings to set the build to unstable.
     */
    public void setBuildUnstableTotalNormal(String threshold) {
        ensureAdvancedClicked();
        buildUnstableTotalNormal.set(threshold);
    }

    /**
     * Build is marked as unstable if at least these warnings of low priority are found.
     *
     * @param threshold number of warnings to set the build to unstable.
     */
    public void setBuildUnstableTotalLow(String threshold) {
        ensureAdvancedClicked();
        buildUnstableTotalLow.set(threshold);
    }

    /**
     * Build is marked as failed if at least these warnings are found.
     *
     * @param threshold number of warnings to set the build to failed.
     */
    public void setBuildFailedTotalAll(String threshold) {
        ensureAdvancedClicked();
        buildFailedTotalAll.set(threshold);
    }

    /**
     * Build is marked as failed if at least these warnings of high priority are found.
     *
     * @param threshold number of warnings to set the build to unstable.
     */
    public void setBuildFailedTotalHigh(String threshold) {
        ensureAdvancedClicked();
        buildFailedTotalHigh.set(threshold);
    }

    /**
     * Build is marked as failed if at least these warnings of normal priority are found.
     *
     * @param threshold number of warnings to set the build to unstable.
     */
    public void setBuildFailedTotalNormal(String threshold) {
        ensureAdvancedClicked();
        buildFailedTotalNormal.set(threshold);
    }

    /**
     * Build is marked as failed if at least these warnings of low priority are found.
     *
     * @param threshold number of warnings to set the build to unstable.
     */
    public void setBuildFailedTotalLow(String threshold) {
        ensureAdvancedClicked();
        buildFailedTotalLow.set(threshold);
    }

    /**
     * Build is marked as failed if at least these new warnings are found.
     *
     * @param threshold number of new warnings to set the build to failed.
     */
    public void setNewWarningsThresholdFailed(final String threshold) {
        setNewWarningsThresholdFailed(threshold, false);
    }

    /**
     * Build is marked as failed if at least these new warnings are found.
     *
     * @param threshold              number of new warnings to set the build to failed.
     * @param usePreviousAsReference determines if the delta computation should use the previous build rather than the
     *                               reference build
     */
    public void setNewWarningsThresholdFailed(final String threshold, final boolean usePreviousAsReference) {
        ensureAdvancedClicked();
        canComputeNew.check(true);
        newWarningsThresholdFailed.set(threshold);
        usePreviousBuild.check(usePreviousAsReference);
    }

    /**
     * Build is marked as unstable if at least these new warnings are found.
     *
     * @param threshold number of new warnings to set the build to unstable.
     */
    public void setNewWarningsThresholdUnstable(final String threshold) {
        setNewWarningsThresholdUnstable(threshold, false);
    }

    /**
     * Build is marked as unstable if at least these new warnings are found.
     *
     * @param threshold number of new warnings to set the build to unstable.
     * @param usePreviousAsReference determines if the delta computation should use the previous build rather than the
     *                               reference build
     */
    public void setNewWarningsThresholdUnstable(final String threshold, final boolean usePreviousAsReference) {
        ensureAdvancedClicked();
        canComputeNew.check(true);
        newWarningsThresholdUnstable.set(threshold);
        usePreviousBuild.check(usePreviousAsReference);
    }

    /**
     * Detect new warnings in comparison with the last build.
     *
     * @param deltaValues compare if true
     */
    public void setUseDeltaValues(boolean deltaValues) {
        ensureAdvancedClicked();
        canComputeNew.check(true);
        useDeltaValues.check(deltaValues);
    }

    /**
     * Decides if the code analyzer shall run if the build has failed.
     *
     * @param canRun build if true.
     */
    public void setCanRunOnFailed(boolean canRun) {
        ensureAdvancedClicked();
        canRunOnFailed.check(canRun);
    }

     /**
     * Decides if the code analyzer should resolve relative paths.
     *
     * @param canResolve build if true.
     */
    public void setCanResolveRelativePaths(boolean canResolve) {
        ensureAdvancedClicked();
        canResolveRelativePaths.check(canResolve);
    }

    /**
     * Ensures that advanced is clicked and the other controls are visible.
     */
    protected void ensureAdvancedClicked() {
        if (advanced.exists()) {
            advanced.click();
        }
    }

    /**
     * Returns the repeatable add button for the specified property.
     *
     * @param propertyName the name of the repeatable property
     * @return the selected repeatable add button
     */
    protected Control findRepeatableAddButtonFor(final String propertyName) {
        return control(by.xpath("//div[@id='" + propertyName + "']//button[contains(@path,'repeatable-add')]"));
    }

    /**
     * Returns the repeatable delete buttons for the specified property.
     *
     * @param propertyName the name of the repeatable property
     * @return the selected repeatable delete buttons
     */
    protected List<WebElement> findRepeatableDeleteButtonsFor(final String propertyName) {
        return all(by.xpath("//div[@id='" + propertyName + "']//button[contains(@path,'repeatable-delete')]"));
    }
}
