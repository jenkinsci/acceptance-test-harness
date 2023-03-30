/*
 * The MIT License
 *
 * Copyright (c) 2023 CloudBees, Inc.
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
package org.jenkinsci.test.acceptance.utils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;
import org.jenkinsci.test.acceptance.po.JobsMixIn;
import org.jenkinsci.test.acceptance.po.WorkflowJob;
import org.junit.Assert;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

public class PipelineTestUtils {

    public static WorkflowJob createPipelineJobWithScript(final JobsMixIn jobs, final String script) {
        final WorkflowJob job = jobs.create(WorkflowJob.class);

        job.script.set(script);
        job.save();

        return job;
    }

    public static String scriptForPipelineFromResource(final Class resourceOwner, final String resourceName) throws IOException {
        String resourceDirName = resourceOwner.getSimpleName().toLowerCase(Locale.ENGLISH).replace("test", "");
        return IOUtils.toString(resourceOwner.getResourceAsStream( "/" + resourceDirName + "/" + resourceName), StandardCharsets.UTF_8);
    }

    public static String scriptForPipelineFromResourceWithParameters(final Class resourceOwner, final String resourceName, final String... scriptParameters) throws IOException {
        final String script = scriptForPipelineFromResource(resourceOwner, resourceName);
        checkScript(script);

        return String.format(script, scriptParameters);
    }

    public static void checkScript(final String script) {
        assertThat(script, is(not(nullValue())));
    }

    public static String resolveScriptName(final String scriptName) {
        return (SystemUtils.IS_OS_UNIX) ? scriptName : scriptName + "Windows";
    }
}
