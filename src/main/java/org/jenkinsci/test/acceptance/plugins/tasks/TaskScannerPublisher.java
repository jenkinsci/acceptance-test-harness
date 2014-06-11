package org.jenkinsci.test.acceptance.plugins.tasks;

import org.jenkinsci.test.acceptance.plugins.AbstractCodeStylePluginPostBuildStep;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.Job;

/**
 * This class provides the ability to add a 'Scan workspace for open tasks'
 * post build step to a job.
 *
 * It provides access to the particular controls to configure the post build step.
 * This class derives some common controls from
 * {@link org.jenkinsci.test.acceptance.plugins.AbstractCodeStylePluginPostBuildStep}.
 *
 * This post build step requires installation of the tasks plugin.
 *
 * @author Martin Ende
 */

@Describable("Scan workspace for open tasks")
public class TaskScannerPublisher extends AbstractCodeStylePluginPostBuildStep {

    /**
     * The input for the file names to be excluded
     */
    public final Control excludePattern = control("excludePattern");

    /**
     * The input for the high priority task tags.
     */
    public final Control highPriorityTags = control("high");

    /**
     * The input for the normal priority task tags.
     */
    public final Control normalPriorityTags = control("normal");

    /**
     * The input for the low priority task tags.
     */
    public final Control lowPriorityTags = control("low");

    /**
     * Checkbox to ignore the case when searching for task tags
     */
    public final Control ignoreCase = control("ignoreCase");

    /**
     * Checkbox to run the plug-in also for failed builds
     */
    public final Control runAlways = control("canRunOnFailed");

    /**
     * Checkbox whether Ant or Maven modules should be detected
     */
    public final Control detectModules = control("shouldDetectModules");

    /**
     * Input for the task threshold, which marks the build as healthy
     */
    public final Control healthyThreshold = control("healthy");

    /**
     * Input for the task threshold, which marks the build as unhealthy
     */
    public final Control unhealthyThreshold = control("unHealthy");

    /**
     * Radio button to consider only high priority warnings are considered for the build's health
     */
    public final Control onlyHighPriorityForHealth = control("thresholdLimit[high]");

    /**
     * Radio button to consider high and normal priority warnings are considered for the build's health
     */
    public final Control highAndNormalPriorityForHealth = control("thresholdLimit[normal]");

    /**
     * Radio button to consider all priority warnings are considered for the build's health
     */
    public final Control allPrioritiesForHealth = control("thresholdLimit[low]");

    /**
     * Input for high priority warning threshold, which when exceeded mark the build as unstable.
     */
    public final Control warningThresholdHighUnstable = control("unstableTotalHigh");

    /**
     * Input for normal priority warning threshold, which when exceeded mark the build as unstable.
     */
    public final Control warningThresholdNormalUnstable = control("unstableTotalNormal");

    /**
     * Input for low priority warning threshold, which when exceeded mark the build as unstable.
     */
    public final Control warningThresholdLowUnstable = control("unstableTotalLow");

    /**
     * Input for warning threshold, which when exceeded mark the build as failed.
     */
    public final Control warningThresholdFailed = control("failedTotalAll");

    /**
     * Input for high priority warning threshold, which when exceeded mark the build as failed.
     */
    public final Control warningThresholdHighFailed = control("failedTotalHigh");

    /**
     * Input for normal priority warning threshold, which when exceeded mark the build as failed.
     */
    public final Control warningThresholdNormalFailed = control("failedTotalNormal");

    /**
     * Input for low priority warning threshold, which when exceeded mark the build as failed.
     */
    public final Control warningThresholdLowFailed = control("failedTotalLow");

    /**
     * Input for warning threshold, when computing delta warnings.
     */
    public final Control newWarningsThresholdUnstable = control("canComputeNew/unstableNewAll");

    /**
     * Input for high priority warning threshold, when computing delta warnings.
     */
    public final Control newWarningsThresholdHighUnstable = control("canComputeNew/unstableNewHigh");

    /**
     * Input for normal priority warning threshold, when computing delta warnings.
     */
    public final Control newWarningsThresholdNormalUnstable = control("canComputeNew/unstableNewNormal");

    /**
     * Input for low priority warning threshold, when computing delta warnings.
     */
    public final Control newWarningsThresholdLowUnstable = control("canComputeNew/unstableNewLow");

    /**
     * Input for high priority warning threshold, when computing delta warnings.
     */
    public final Control newWarningsThresholdHighFailed = control("canComputeNew/failedNewHigh");

    /**
     * Input for normal priority warning threshold, when computing delta warnings.
     */
    public final Control newWarningsThresholdNormalFailed = control("canComputeNew/failedNewNormal");

    /**
     * Input for low priority warning threshold, when computing delta warnings.
     */
    public final Control newWarningsThresholdLowFailed = control("canComputeNew/failedNewLow");

    /**
     * Checkbox to use the last stable build as reference when calculating the number of new warnings
     */
    public final Control useStableBuildAsReference = control("canComputeNew/useStableBuildAsReference");




    public TaskScannerPublisher(Job parent, String path) { super(parent, path); }
}
