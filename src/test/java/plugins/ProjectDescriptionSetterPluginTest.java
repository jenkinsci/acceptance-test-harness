package plugins;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.project_description_setter.ProjectDescriptionSetter;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.junit.Test;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

@WithPlugins({
        "matrix-project", // JENKINS-37545
        "project-description-setter"
})
public class ProjectDescriptionSetterPluginTest extends AbstractJUnitTest {
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
