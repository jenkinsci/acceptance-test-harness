package plugins;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.description_setter.BuildDescriptionSetter;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.junit.Test;

@WithPlugins({
        "matrix-project", // JENKINS-37545
        "description-setter"
})
public class DescriptionSetterPluginTest extends AbstractJUnitTest {

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
        assertThat(find(by.css("#buildHistoryPage .desc, #buildHistoryPage .app-builds-container__item__description")).getText(), is(equalTo(msg)));
    }
}
