package org.jenkinsci.test.acceptance.plugins.gradle;

import org.jenkinsci.test.acceptance.po.*;

public class GradleWrapper {

    public static void downloadWrapperFiles(final Job job){
        final GradleStep wrapperDownloadStep = job.addBuildStep(GradleStep.class);
        wrapperDownloadStep.setTasks("wrapper");
    }

    public static void addWrapperStep(final Job job, final String wrapperLocation, final String tasks){
        final GradleStep wrapperExecutionStep = job.addBuildStep(GradleStep.class);
        wrapperExecutionStep.setUseWrapper();
        wrapperExecutionStep.setMakeWrapperExecutable();
        if(tasks != null) {
            wrapperExecutionStep.setTasks(tasks);
        }
        if(wrapperLocation != null) {
            wrapperExecutionStep.setWrapperLocation(wrapperLocation);
        }

    }

    public static void moveWrapperFiles(final Job job, final String wrapperLocation){
        final ShellBuildStep step = job.addBuildStep(ShellBuildStep.class);
        step.command("mkdir -p " + wrapperLocation + "; " +
                "mv gradle " + wrapperLocation + "; " +
                "mv gradlew " + wrapperLocation + "; " +
                "mv gradlew.bat " + wrapperLocation);
    }

}
