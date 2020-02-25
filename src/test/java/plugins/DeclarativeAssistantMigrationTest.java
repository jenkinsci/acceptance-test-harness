package plugins;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.ShellBuildStep;
import org.jenkinsci.test.acceptance.slave.SlaveController;
import org.junit.Test;
import org.openqa.selenium.By;

import javax.inject.Inject;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.jenkinsci.test.acceptance.Matchers.hasElement;

//@WithPlugins({"trilead-api@1.0.5","credentials@2.1.16","credentials-binding@1.13","git@4.1.0", // ,"ssh-credentials@1.18.1"
//                "pipeline-model-api","pipeline-model-definition","scm-api","declarative-pipeline-migration-assistant",
//                "declarative-pipeline-migration-assistant-api"})
@WithPlugins({"declarative-pipeline-migration-assistant","declarative-pipeline-migration-assistant-api"})
public class DeclarativeAssistantMigrationTest
    extends AbstractJUnitTest {

    @Test @WithPlugins({"trilead-api@1.0.5","git"})
    public void basicDeclarativeTests() throws Exception {
        FreeStyleJob j = jenkins.jobs.create(FreeStyleJob.class, "simple-job-to-declarative");
        j.configure();
        ShellBuildStep shell = j.addBuildStep( ShellBuildStep.class);
        shell.command("echo 1");

        j.apply();
        j.save();

        try
        {
            clickLink("To Declarative");
            assertThat(driver, hasElement(By.className( "rectangle-conversion-success" )));
        }
        finally
        {
            //sleep( 20000 );
        }

    }
}
