/*
 * The MIT License
 *
 * Copyright (c) 2014 Red Hat, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.jenkinsci.test.acceptance.plugins.gradle;

import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.Job;

@Describable("Configure gradle wrapper step")
public class GradleWrapper {

    final static String gradleDir = "/gradle_plugin/";
    final static String wrapperDir = gradleDir + "wrapper/";

    private static void addWrapperFiles(final Job job){
        job.copyResource(job.resource(gradleDir + "script.gradle"), "build.gradle");
        job.copyResource(job.resource(wrapperDir + "gradlew"), "gradlew");
        job.copyResource(job.resource(wrapperDir + "gradlew.bat"), "gradlew.bat");
        job.copyResource(job.resource(wrapperDir + "gradle-wrapper.jar"), "gradle/wrapper/gradle-wrapper.jar");
        job.copyResource(job.resource(wrapperDir + "gradle-wrapper.properties"), "gradle/wrapper/gradle-wrapper.properties");
    }

    public static void addWrapperStep(final Job job){
        addWrapperFiles(job);
        final GradleStep step = job.addBuildStep(GradleStep.class);
        step.useWrapper.click();
        step.makeWrapperExecutable.click();
    }
}
