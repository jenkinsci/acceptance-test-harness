//package org.jenkinsci.test.acceptance.plugins;
//
//import org.jenkinsci.test.acceptance.po.AbstractStep;
//import org.jenkinsci.test.acceptance.po.Control;
//import org.jenkinsci.test.acceptance.po.Job;
//import org.jenkinsci.test.acceptance.po.PostBuildStep;
//
///**
// * Abstract class to use for post build steps.
// *
// * @author Martin Kurz
// */
//public abstract class AbstractCodeStylePluginPostBuildStep extends AbstractStep implements PostBuildStep {
//    /**
//     * Button, which opens advanced settings.
//     */
//    public final Control advanced = control("advanced-button");
//    /**
//     * Input for warning threshold, which when exceeded mark the build as unstable.
//     */
//    public final Control warningThresholdUnstable = control("unstableTotalAll");
//    /**
//     * Checkbox to enable computation of new warnings compared with old build.
//     */
//    public final Control computeNewWarningsComparedWithReferenceBuild = control("canComputeNew");
//    /**
//     * Input for warning threshold when computing delta warnings.
//     */
//    public final Control newWarningsThresholdFailed = control("canComputeNew/failedNewAll");
//    /**
//     * Checkbox to enable delta warnings
//     */
//    public final Control useDeltaValues = control("canComputeNew/useDeltaValues");
//
//    public AbstractCodeStylePluginPostBuildStep(Job parent, String path) {
//        super(parent, path);
//    }
//
//    public Control getPattern() {
//        return pattern;
//    }
//}
