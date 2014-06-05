package plugins;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.cobertura.CoberturaAction;
import org.jenkinsci.test.acceptance.plugins.cobertura.CoberturaPublisher;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.jenkinsci.test.acceptance.Matchers.hasAction;

/**
 * Feature: Allow publishing of Cobertura analysis
 * In order to be able track test coverage of my project
 * As a Jenkins user
 * I want to be able to publish Cobertura analysis report
 */
@WithPlugins("cobertura")
public class CoberturaPluginTest extends AbstractJUnitTest {
    /**
     * Scenario: Record Cobertura coverage report
     * Given I have installed the "cobertura" plugin
     * And a job
     * When I configure the job
     * And I copy resource "cobertura_plugin/coverage.xml" into workspace
     * And I add "Publish Cobertura Coverage Report" post-build action
     * And I set up "coverage.xml" as the Cobertura report
     * And I save the job
     * And I build the job
     * Then the build should have "Coverage Report" action
     * And the job should have "Coverage Report" action
     */
    @Test
    public void record_covertura_coverage_report() {
        FreeStyleJob j = setupJob();

        Build b = j.startBuild().waitUntilFinished();
        assertThat(b, hasAction("Coverage Report"));
        assertThat(j, hasAction("Coverage Report"));
    }

    private FreeStyleJob setupJob() {
        FreeStyleJob j = jenkins.jobs.create();
        j.configure();
        {
            j.copyResource(resource("/cobertura_plugin/coverage.xml"));
            CoberturaPublisher c = j.addPublisher(CoberturaPublisher.class);
            c.reportFile.set("coverage.xml");
        }
        j.save();
        return j;
    }

    /**
     * Scenario: View Cobertura coverage report
     * Given I have installed the "cobertura" plugin
     * And a job
     * When I configure the job
     * And I copy resource "cobertura_plugin/coverage.xml" into workspace
     * And I add "Publish Cobertura Coverage Report" post-build action
     * And I set up "coverage.xml" as the Cobertura report
     * And I save the job
     * And I build the job
     * Then the build should succeed
     * When I visit Cobertura report
     * Then I should see the coverage of packages is 100%
     * Then I should see the coverage of files is 50%
     * Then I should see the coverage of classes is 31%
     * Then I should see the coverage of methods is 23%
     * Then I should see the coverage of lines is 16%
     * Then I should see the coverage of conditionals is 10%
     */
    @Test
    public void view_cobertura_coverage_report() {
        FreeStyleJob j = setupJob();

        Build b = j.startBuild().waitUntilFinished();
        CoberturaAction a = new CoberturaAction(b);

        assertThat(a.getPackageCoverage(), is(100));
        assertThat(a.getFilesCoverage(), is(50));
        assertThat(a.getClassesCoverage(), is(31));
        assertThat(a.getMethodsCoverage(), is(23));
        assertThat(a.getLinesCoverage(), is(16));
        assertThat(a.getConditionalsCoverage(), is(10));
    }
}
