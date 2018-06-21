package plugins;

import java.util.List;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.warnings.IssuesRecorder;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.MessageBox;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Alexander Praegla, Nikolai Wohlgemuth, Arne Schöntag
 */
@WithPlugins({"warnings", "checkstyle"})
public class MessageBoxTest extends AbstractJUnitTest {
    private static final String WARNINGS_PLUGIN_PREFIX = "/warnings_plugin/white-mountains/";
    private static final String WARNINGS_XML = "checkstyle-result.xml";

    private static final String CHECKSTYLE_PLUGIN_ROOT = WARNINGS_PLUGIN_PREFIX + "message_box/";

    @Test
    public void shouldBeOkIfContentsOfMsgBoxesAreCorrect() {

        FreeStyleJob job = createFreeStyleJob(WARNINGS_XML);
        job.addPublisher(IssuesRecorder.class, recorder -> {
            recorder.setTool("CheckStyle", "**/checkstyle-result.xml");
            recorder.setEnabledForAggregation(false);
        });
        job.save();
        job.startBuild().waitUntilFinished();

        MessageBox messageBox = new MessageBox(job);
        messageBox.open();

        // Check Error Panel
        List<String> errors = messageBox.getErrorMsgContent();
        String errno1 = "Can't read file '/mnt/hudson_workspace/workspace/HTS-CheckstyleTest/ssh-slaves"
                + "/src/main/java/hudson/plugins/sshslaves/RemoteLauncher.java': java.nio.file.NoSuchFileException:"
                + " \\mnt\\hudson_workspace\\workspace\\HTS-CheckstyleTest\\ssh-slaves\\src\\main\\java\\hudson\\plugins"
                + "\\sshslaves\\RemoteLauncher.java";
        Assert.assertTrue(errors.get(1).contains(errno1));

        // Check Info Panel
        List<String> infos = messageBox.getInfoMsgContent();
        Assert.assertTrue(infos.get(1).contains("found 1 file"));
        Assert.assertTrue(infos.get(3).contains("for 2 issues"));
        Assert.assertTrue(infos.get(6).contains("No quality gates have been set - skipping"));
    }

    private FreeStyleJob createFreeStyleJob(final String... resourcesToCopy) {
        FreeStyleJob job = jenkins.getJobs().create(FreeStyleJob.class);
        for (String resource : resourcesToCopy) {
            job.copyResource(resource(CHECKSTYLE_PLUGIN_ROOT + resource));
        }
        return job;
    }
}
