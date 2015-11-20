package plugins;

import org.hamcrest.MatcherAssert;
import org.jenkinsci.test.acceptance.Matchers;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.po.FolderJob;
import org.junit.Test;

/**
 * Acceptance tests for the CloudBees Folder Plugins.
 * @author Andres Rodriguez
 */
@WithPlugins("cloudbees-folder")
public class FolderPluginTest extends AbstractJUnitTest {
    /** Test folder name. */
    private static final String F01 = "F01";
    
    /**
     * First simple test scenario: Folder creation (JENKINS-31648)
     *
     * - We create a folder named "F01".
     * - We check the folder exists and we can enter it.
     */
    @Test
    public void createFolder() throws Exception {
        // 1 - Create job
        final FolderJob job = jenkins.jobs.create(FolderJob.class, F01);
        // 2 - Save job
        job.save();
    	// 3 - Go to main page.
        jenkins.open();
    	// 4 - Go to job page.
        job.open();
        // Ensure the folder name is correct.
    	MatcherAssert.assertThat(driver, Matchers.hasContent(F01));
    }
}
