package plugins;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.pmd.PmdPublisher;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.junit.Before;
import org.junit.Test;

import static org.jenkinsci.test.acceptance.Matchers.*;

/**
 Feature: Tests for PMD plugin
 */
@WithPlugins("pmd")
public class PmdPluginTest extends AbstractJUnitTest {

    private FreeStyleJob j;

    @Before
    public void setUp() {
        j = jenkins.jobs.create();
    }

    /**
     Scenario: Configure a job with PMD post-build steps
       Given I have installed the "pmd" plugin
       And a job
       When I configure the job
       And I add "Publish PMD analysis results" post-build action
       And I copy resource "pmd_plugin/pmd.xml" into workspace
       And I set path to the pmd result "pmd.xml"
       And I save the job
       And I build the job
       Then the build should succeed
       And build page should has pmd summary "0 warnings"
     */
    @Test
    public void configure_ajob_with_PMD_post_build_steps() {
        j.configure();
        j.copyResource(resource("/pmd_plugin/pmd.xml"));
        PmdPublisher pmd = j.addPublisher(PmdPublisher.class);
        pmd.advanced.click();
        pmd.pattern.set("pom.xml");
        j.save();

        Build b = j.queueBuild().waitUntilFinished().shouldSucceed();

        assertThat(b.open(), hasContent("0 warnings"));
    }
}
