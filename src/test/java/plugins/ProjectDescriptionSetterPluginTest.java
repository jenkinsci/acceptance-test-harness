package plugins;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.project_description_setter.ProjectDescriptionSetter;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Feature: Set project description based on file in workspace
 * In order to be able to change project description dynamically based upon build output
 * As a Jenkins user
 * I want to be able to set project description using a file in the workspace
 */
@WithPlugins("project-description-setter")
public class ProjectDescriptionSetterPluginTest extends AbstractJUnitTest {
    /**
     * Scenario: Set project description based upon file in workspace
     * Given I have installed the "project-description-setter" plugin
     * And a job
     * When I configure the job
     * And I add a shell build step "echo 'Project description setter test' > desc.txt"
     * And I setup project description from the file "desc.txt" in workspace
     * And I save the job
     * And I build the job
     * Then the job should have description "Project description setter test"
     */
    @Test
    public void set_project_description_based_upon_file_in_workspace() {
        FreeStyleJob j = jenkins.jobs.create();
        j.configure();
        j.addShellStep("echo 'Project description setter test' > desc.txt");
        ProjectDescriptionSetter w = j.addBuildWrapper(ProjectDescriptionSetter.class);
        w.filename.set("desc.txt");
        j.save();

        j.startBuild().shouldSucceed();
        j.open();
        String t = find(by.xpath("//div[@id=\"description\"]/div")).getText();
        assertThat(t, containsString("Project description setter test"));
    }
}
