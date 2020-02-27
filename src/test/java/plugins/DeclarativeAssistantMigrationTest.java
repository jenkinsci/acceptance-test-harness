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
import static org.jenkinsci.test.acceptance.Matchers.containsString;
import static org.jenkinsci.test.acceptance.Matchers.hasElement;

@WithPlugins({"declarative-pipeline-migration-assistant@1.0.3","declarative-pipeline-migration-assistant-api@1.0.3"})
public class DeclarativeAssistantMigrationTest
    extends AbstractJUnitTest {

    @Test //@WithPlugins({"git"})
    public void basicDeclarativeTests() throws Exception {
        FreeStyleJob j = jenkins.jobs.create(FreeStyleJob.class, "simple-job-to-declarative");
        j.configure();
        ShellBuildStep shell = j.addBuildStep( ShellBuildStep.class);
        shell.command("echo 1");

        j.apply();
        j.save();

        try {
            clickLink("To Declarative");
            assertThat(driver, hasElement(By.className("rectangle-conversion-success")));
            assertThat(driver, hasElement(By.className("review-converted")));
            assertThat(driver, hasElement(By.id("jenkinsfile-content")));
            String jenkinsFile =  driver.findElement(By.id("jenkinsfile-content")).getAttribute("value");
            assertThat(jenkinsFile, containsString( "echo 1" ));
        } finally {
            //sleep( 20000 );
        }
    }
}
