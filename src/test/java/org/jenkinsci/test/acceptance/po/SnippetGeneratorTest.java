package org.jenkinsci.test.acceptance.po;

import org.junit.Test;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

@WithPlugins("pipeline-model-definition")
public class SnippetGeneratorTest extends AbstractJUnitTest {
    @Test
    public void createPipelineSnippetToArchiveArtifacts() {
        WorkflowJob job = jenkins.getJobs().create(WorkflowJob.class);
        job.save();

        SnippetGenerator snippetGenerator = new SnippetGenerator(job);
        snippetGenerator.open();

        assertThat(snippetGenerator.generateScript(), is("archiveArtifacts artifacts: '', followSymlinks: false"));
    }
}
