package org.jenkinsci.test.acceptance.plugins.tasks;

import org.jenkinsci.test.acceptance.plugins.AbstractCodeStylePluginBuildSettings;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Job;

/**
 * Abstract class for tasks plugin configuration.
 *
 * It provides access to the particular controls to configure the post build step.
 * This class derives conrtols common to all static analyser plugins from
 * {@link org.jenkinsci.test.acceptance.plugins.AbstractCodeStylePluginBuildSettings}
 * and adds task scanner specific controls.
 *
 * This post build step requires installation of the tasks plugin.
 *
 * @author Martin Ende
 */

public abstract class AbstractTaskScannerBuildSettings extends AbstractCodeStylePluginBuildSettings {

    protected Control pattern = control("pattern");
    protected Control excludePattern = control("excludePattern");

    protected Control highPriorityTags = control("high");
    protected Control normalPriorityTags = control("normal");
    protected Control lowPriorityTags = control("low");
    protected Control ignoreCase = control("ignoreCase");

    protected Control shouldDetectModules = control("shouldDetectModules");

    protected Control buildUnstableNewAll = control("canComputeNew/unstableNewAll");
    protected Control buildUnstableNewHigh = control("canComputeNew/unstableNewHigh");
    protected Control buildUnstableNewNormal = control("canComputeNew/unstableNewNormal");
    protected Control buildUnstableNewLow = control("canComputeNew/unstableNewLow");
    protected Control buildFailedNewHigh = control("canComputeNew/failedNewHigh");
    protected Control buildFailedNewNormal = control("canComputeNew/failedNewNormal");
    protected Control buildFailedNewLow = control("canComputeNew/failedNewLow");

    protected Control useStableBuildAsReference = control("canComputeNew/useStableBuildAsReference");

    public AbstractTaskScannerBuildSettings(Job parent, String path) { super(parent, path); }


    /**
     * Sets the input for the file names to be scanned for tasks.
     */
    public void setPattern(String pattern){
        this.pattern.set(pattern);
    }

    /**
     * Sets the input for the file names to be excluded
     */
    public void setExcludePattern(String pattern) {
        this.excludePattern.set(pattern);
    }

    /**
     * Sets the input for the high priority task tags.
     */
    public void setHighPriorityTags() {
        this.highPriorityTags = null;
    }

    /**
     * Sets the input for the normal priority task tags.
     */
    public void setNormalPriorityTags() {
        this.normalPriorityTags = null;
    }

    /**
     * Sets the input for the low priority task tags.
     */
    public void setLowPriorityTags() {
        this.lowPriorityTags = null;
    }

    /**
     * Decides whether to ignore the case when searching for task tags
     */
    public void setIgnoreCase(boolean ignore) {
        this.ignoreCase.check(ignore);
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
    public void setBuildUnstableNewAll() {
        ensureAdvancedClicked();
        canComputeNew.check(true);
        this.buildUnstableNewAll = null;
    }

    /**
     * Sets the input for high priority warning threshold, when computing delta warnings.
     */
    public void setBuildUnstableNewHigh() {
        ensureAdvancedClicked();
        canComputeNew.check(true);
        this.buildUnstableNewHigh = null;
    }

    /**
     * Sets the input for normal priority warning threshold, when computing delta warnings.
     */
    public void setBuildUnstableNewNormal() {
        ensureAdvancedClicked();
        canComputeNew.check(true);
        this.buildUnstableNewNormal = null;
    }

    /**
     * Sets the input for low priority warning threshold, when computing delta warnings.
     */
    public void setBuildUnstableNewLow() {
        ensureAdvancedClicked();
        canComputeNew.check(true);
        this.buildUnstableNewLow = null;
    }

    /**
     * Sets the input for high priority warning threshold, when computing delta warnings.
     */
    public void setBuildFailedNewHigh() {
        ensureAdvancedClicked();
        canComputeNew.check(true);
        this.buildFailedNewHigh = null;
    }

    /**
     * Sets the input for normal priority warning threshold, when computing delta warnings.
     */
    public void setBuildFailedNewNormal() {
        ensureAdvancedClicked();
        canComputeNew.check(true);
        this.buildFailedNewNormal = null;
    }

    /**
     * Sets the input for low priority warning threshold, when computing delta warnings.
     */
    public void setBuildFailedNewLow() {
        ensureAdvancedClicked();
        canComputeNew.check(true);
        this.buildFailedNewLow = null;
    }

    /**
     * Decides whether to use the last stable build as reference when calculating the number of new warnings
     */
    public void setUseStableBuildAsReference() {
        ensureAdvancedClicked();
        canComputeNew.check(true);
        this.useStableBuildAsReference = null;
    }
}
