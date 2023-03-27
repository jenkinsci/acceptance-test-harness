package org.jenkinsci.test.acceptance;

import org.apache.commons.io.IOUtils;
import org.jenkinsci.test.acceptance.po.JobsMixIn;
import org.jenkinsci.test.acceptance.po.WorkflowJob;
import org.junit.Assert;

import java.io.IOException;

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
        return IOUtils.toString(resourceOwner.getResourceAsStream(resourceName));
    }

    public static String scriptForPipelineFromResourceWithParameters(final Class resourceOwner, final String resourceName, final String... scriptParameters) throws IOException {
        final String script = scriptForPipelineFromResource(resourceOwner, resourceName);
        checkScript(script);

        return String.format(script, scriptParameters);
    }

    public static void checkScript(final String script) {
        Assert.assertThat(script, is(not(nullValue())));
    }
}