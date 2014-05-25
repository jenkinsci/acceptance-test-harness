package groovy.plugins

import org.jenkinsci.test.acceptance.geb.GebSpec
import org.jenkinsci.test.acceptance.junit.WithPlugins
import org.jenkinsci.test.acceptance.plugins.pmd.PmdGebPublisher
import org.jenkinsci.test.acceptance.po.Build
import org.jenkinsci.test.acceptance.po.Job

/**
 *
 * @author christian.fritz
 */
@WithPlugins("pmd")
class PmdPluginTest extends GebSpec {

    private Job j;

    /**
     * Scenario Outline: Check PMD Results
     Given I have installed the "pmd" plugin
     And a job
     When I configure the job
     And I copy resource "pmd_plugin/<pmdfile>" into workspace
     And I add "Publish PMD analysis results" post-build action
     And I set up "<pmdfile>" as the PMD results
     And I set up <threAll> as the <threType> All priorities
     And I set up <threHigh> as the <threType> Priority high
     And I set up <threNormal> as the <threType> Priority normal
     And I set up <threLow> as the <threType> Priority low
     And I save the job
     And I build the job
     Then the build should <buildResult>
     And the build should have "PMD Warnings" action
     And the job should have "PMD Warnings" action
     And the job's analisys summary should have <warnHigh> HIGH warnings
     And the job's analisys summary should have <warnNormal> NORMAL warnings
     And the job's analisys summary should have <warnLow> LOW warnings
     And some build tokens like fixed and new are available
     */
    def "Check PMD Results"() {
        given: "I have installed the 'pmd' plugin and a job"
        j = jenkins.jobs.create();
        j.copyResource(resource("/pmd_plugin/pmd.xml"));
        PmdGebPublisher pmd = j.addPublisher(PmdGebPublisher.class);
        pmd.pattern.set("pmd.xml");
        when: "I configure the job"

        j.save();

        Build b = j.startBuild().waitUntilFinished().shouldSucceed();
        then: "the build shoud failing and have the following warnings: 15 high, 25 normal and 13 low"
    }
}
