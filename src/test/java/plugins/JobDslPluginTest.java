package plugins;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.job_dsl.*;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Acceptance tests for the Job DSL plugin.
 *
 * @author Maximilian Oeckler
 */
@WithPlugins("job-dsl")
public class JobDslPluginTest extends AbstractJUnitTest {

    /**
     * Tests if the checkbox ignoreMissingFiles is shown when the
     * radiobutton 'Look on Filesystem' is selected,
     * and not shown if the radiobutton 'Use the provided DSL script'
     * is selected.
     */
    @Test
    public void is_ignoreMissingFiles_shown_right() {
        FreeStyleJob seed = jenkins.jobs.create(FreeStyleJob.class);
        JobDslBuildStep jobdsl = seed.addBuildStep(JobDslBuildStep.class);
        assertTrue(jobdsl.isIgnoreMissingFilesShown());
        jobdsl.clickUseScriptText();
        assertFalse(jobdsl.isIgnoreMissingFilesShown());
        jobdsl.clickLookOnFilesystem();
        assertTrue(jobdsl.isIgnoreMissingFilesShown());
    }
}
