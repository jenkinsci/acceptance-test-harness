package org.jenkinsci.test.acceptance.plugins.tasks;

import org.jenkinsci.test.acceptance.plugins.analysis_core.AnalysisSettings;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Job;

/**
 * Abstract class for tasks plugin configuration.
 *
 * It provides access to the particular controls to configure the post build step.
 * This class derives controls common to all static analyser plugins from
 * {@link org.jenkinsci.test.acceptance.plugins.analysis_core.AnalysisSettings}
 * and adds task scanner specific controls.
 *
 * This post build step requires installation of the tasks plugin.
 *
 * @author Martin Ende
 */
// FIXME: move up to analysis settings
public abstract class AbstractTaskScannerBuildSettings extends AnalysisSettings {
    protected Control pattern = control("pattern");
    protected Control excludePattern = control("excludePattern");

    protected Control highPriorityTags = control("high");
    protected Control normalPriorityTags = control("normal");
    protected Control lowPriorityTags = control("low");
    protected Control ignoreCase = control("ignoreCase");
    protected Control asRegexp = control("asRegexp");

    protected Control shouldDetectModules = control("shouldDetectModules");

    protected Control buildUnstableNewAll = control("unstableNewAll", "canComputeNew/unstableNewAll");
    protected Control buildUnstableNewHigh = control("unstableNewHigh", "canComputeNew/unstableNewHigh");
    protected Control buildUnstableNewNormal = control("unstableNewNormal", "canComputeNew/unstableNewNormal");
    protected Control buildUnstableNewLow = control("unstableNewLow", "canComputeNew/unstableNewLow");
    protected Control buildFailedNewHigh = control("failedNewHigh", "canComputeNew/failedNewHigh");
    protected Control buildFailedNewNormal = control("failedNewNormal", "canComputeNew/failedNewNormal");
    protected Control buildFailedNewLow = control("failedNewLow", "canComputeNew/failedNewLow");

    protected Control useStableBuildAsReference = control("useStableBuildAsReference", "canComputeNew/useStableBuildAsReference");

    public AbstractTaskScannerBuildSettings(Job parent, String path) { super(parent, path); }

    /**
     * Sets the input for the file names to be scanned for tasks.
     */
    public void setPattern(String pattern){
        this.pattern.set(pattern);
    }

    /**
     * Sets the input for the file names to be excluded.
     */
    public void setExcludePattern(String pattern) {
        this.excludePattern.set(pattern);
    }

    /**
     * Sets the input for the high priority task tags.
     */
    public void setHighPriorityTags(String tags) {
        this.highPriorityTags.set(tags);
    }

    /**
     * Sets the input for the normal priority task tags.
     */
    public void setNormalPriorityTags(String tags) {
        this.normalPriorityTags.set(tags);
    }

    /**
     * Sets the input for the low priority task tags.
     */
    public void setLowPriorityTags(String tags) {
        this.lowPriorityTags.set(tags);
    }

    /**
     * Decides whether to ignore the case when searching for task tags.
     */
    public void setIgnoreCase(boolean ignore) {
        this.ignoreCase.check(ignore);
    }

    /**
     * Decides whether to treat tags as regular expressions.
     */
    public void setAsRegexp(boolean asRegexp) {
        this.asRegexp.check(asRegexp);
    }

    /**
     * Decides whether Ant or Maven modules should be detected
     */
    public void setShouldDetectModules(boolean detect) {
        ensureAdvancedClicked();
        this.shouldDetectModules.check(detect);
    }

    /**
     * Sets the input for warning threshold, when computing delta warnings.
     */
    public void setBuildUnstableNewAll(String threshold) {
        ensureAdvancedClicked();
        canComputeNew.check(true);
        this.buildUnstableNewAll.set(threshold);
    }

    /**
     * Sets the input for high priority warning threshold, when computing delta warnings.
     */
    public void setBuildUnstableNewHigh(String threshold) {
        ensureAdvancedClicked();
        canComputeNew.check(true);
        this.buildUnstableNewHigh.set(threshold);
    }

    /**
     * Sets the input for normal priority warning threshold, when computing delta warnings.
     */
    public void setBuildUnstableNewNormal(String threshold) {
        ensureAdvancedClicked();
        canComputeNew.check(true);
        this.buildUnstableNewNormal.set(threshold);
    }

    /**
     * Sets the input for low priority warning threshold, when computing delta warnings.
     */
    public void setBuildUnstableNewLow(String threshold) {
        ensureAdvancedClicked();
        canComputeNew.check(true);
        this.buildUnstableNewLow.set(threshold);
    }

    /**
     * Sets the input for high priority warning threshold, when computing delta warnings.
     */
    public void setBuildFailedNewHigh(String threshold) {
        ensureAdvancedClicked();
        canComputeNew.check(true);
        this.buildFailedNewHigh.set(threshold);
    }

    /**
     * Sets the input for normal priority warning threshold, when computing delta warnings.
     */
    public void setBuildFailedNewNormal(String threshold) {
        ensureAdvancedClicked();
        canComputeNew.check(true);
        this.buildFailedNewNormal.set(threshold);
    }

    /**
     * Sets the input for low priority warning threshold, when computing delta warnings.
     */
    public void setBuildFailedNewLow(String threshold) {
        ensureAdvancedClicked();
        canComputeNew.check(true);
        this.buildFailedNewLow.set(threshold);
    }

    /**
     * Decides whether to use the last stable build as reference when calculating the number of new warnings
     */
    public void setUseStableBuildAsReference(boolean useStableBuild) {
        ensureAdvancedClicked();
        canComputeNew.check(true);
        this.useStableBuildAsReference.check(useStableBuild);
    }
}
