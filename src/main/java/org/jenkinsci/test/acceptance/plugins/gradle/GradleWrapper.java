package org.jenkinsci.test.acceptance.plugins.gradle;

import org.jenkinsci.test.acceptance.po.Job;
import org.jenkinsci.test.acceptance.po.ShellBuildStep;

public class GradleWrapper {

    public static void downloadWrapperFiles(final Job job) {
        final GradleStep wrapperDownloadStep = job.addBuildStep(GradleStep.class);
        wrapperDownloadStep.setTasks("wrapper --gradle-version " + GradleInstallation.DEFAULT_VERSION);
        wrapperDownloadStep.setVersion(GradleInstallation.DEFAULT);
        wrapperDownloadStep.setSwitches("--no-daemon");
    }

    public static void addWrapperStep(final Job job, final String wrapperLocation, final String tasks) {
        final GradleStep wrapperExecutionStep = job.addBuildStep(GradleStep.class);
        wrapperExecutionStep.setUseWrapper();
        wrapperExecutionStep.setMakeWrapperExecutable();
        wrapperExecutionStep.setSwitches("--no-daemon");
        if (tasks != null) {
            wrapperExecutionStep.setTasks(tasks);
        }
        if (wrapperLocation != null) {
            wrapperExecutionStep.setWrapperLocation(wrapperLocation);
        }
    }

    public static void moveWrapperFiles(final Job job, final String wrapperLocation) {
        final ShellBuildStep step = job.addBuildStep(ShellBuildStep.class);
        step.command("mkdir -p " + wrapperLocation + "; " + "mv gradle "
                + wrapperLocation + "; " + "mv gradlew "
                + wrapperLocation + "; " + "mv gradlew.bat "
                + wrapperLocation);
    }
}
