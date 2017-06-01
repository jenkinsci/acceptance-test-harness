package org.jenkinsci.test.acceptance.plugins.gradle;

import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.Job;

@Describable("Configure gradle wrapper step")
public class GradleWrapper {

    final static String resourceDir = "/gradle_plugin/wrapper/";

    private static void addWrapperFiles(final Job job){
        job.copyResource(job.resource(resourceDir + "build.gradle"), "build.gradle");
        job.copyResource(job.resource(resourceDir + "gradlew"), "gradlew");
        job.copyResource(job.resource(resourceDir + "gradlew.bat"), "gradlew.bat");
        job.copyResource(job.resource(resourceDir + "gradle-wrapper.jar"), "gradle/wrapper/gradle-wrapper.jar");
        job.copyResource(job.resource(resourceDir + "gradle-wrapper.properties"), "gradle/wrapper/gradle-wrapper.properties");
    }

    public static void addWrapperStep(final Job job){
        addWrapperFiles(job);
        final GradleStep step = job.addBuildStep(GradleStep.class);
        step.useWrapper.click();
        step.makeWrapperExecutable.click();
    }
}
