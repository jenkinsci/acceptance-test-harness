package org.jenkinsci.test.acceptance.po;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.junit.Test;

@WithPlugins("pipeline-model-definition")
public class SnippetGeneratorTest extends AbstractJUnitTest {
    @Test
    public void createPipelineSnippetToArchiveArtifacts() {
        WorkflowJob job = jenkins.getJobs().create(WorkflowJob.class);
        job.save();

        SnippetGenerator snippetGenerator = new SnippetGenerator(job);
        snippetGenerator.open();
        ArtifactArchiver aa = snippetGenerator.selectStep(ArtifactArchiver.class);
        aa.includes("*.bin");
        assertThat(snippetGenerator.generateScript(), is("archiveArtifacts artifacts: '*.bin', followSymlinks: false"));
    }
}
