package plugins;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.description_setter.BuildDescriptionSetter;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/**
 * Feature: Set the description for each build, based upon a RegEx test of the build log file
 * In order to be able to see important build information on project and build page
 * As a Jenkins user
 * I want to be able to set build description based upon regular expression
 */
@WithPlugins("description-setter")
public class DescriptionSetterPluginTest extends AbstractJUnitTest {
    /**
     * Scenario: Set build description based upon build log file
     * Given I have installed the "description-setter" plugin
     * And a job
     * When I configure the job
     * And I add a shell build step "echo '=== test ==='"
     * And I add "Set build description" post-build action
     * And I set up "===(.*)===" as the description setter reg-exp
     * And I set up "Descrption setter test works!" as the description setter description
     * And I save the job
     * And I build the job
     * Then the build should have description "Descrption setter test works!"
     * Then the build should have description "Descrption setter test works!" in build history
     */
    @Test
    public void set_build_description_based_upon_build_log_file() {
        final String msg = "Description setter test works!";

        FreeStyleJob j = jenkins.jobs.create();
        j.configure();
        {
            j.addShellStep("echo '=== test ==='");
            BuildDescriptionSetter s = j.addPublisher(BuildDescriptionSetter.class);
            s.regexp.set("===(.*)===");
            s.description.set(msg);
        }
        j.save();

        Build b = j.startBuild().shouldSucceed();

        b.open();
        assertThat(find(by.css("div#description div")).getText(), is(equalTo(msg)));

        j.open();
        assertThat(find(by.css("#buildHistory .desc")).getText(), is(equalTo(msg)));
    }
}
