package plugins;

import com.google.inject.Inject;
import java.io.IOException;
import java.util.regex.Pattern;
import org.jenkinsci.test.acceptance.docker.fixtures.MailhogContainer;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.DockerTest;
import org.jenkinsci.test.acceptance.junit.WithDocker;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.mailer.Mailer;
import org.jenkinsci.test.acceptance.plugins.mailer.MailerGlobalConfig;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.utils.mail.MailhogProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.openqa.selenium.By;

@WithPlugins("mailer")
@Category(DockerTest.class)
@WithDocker
public class MailerPluginTest extends AbstractJUnitTest {
    @Inject
    private MailhogProvider mailhogProvider;

    private MailhogContainer mailhog;

    @Inject
    private MailerGlobalConfig mailer;

    @Before
    public void setup() {
        mailhog = mailhogProvider.get();
    }

    @Test
    public void send_test_mail() throws IOException {
        jenkins.configure();
        mailer.sendTestMail("admin@example.com");
        mailhog.assertMail(
                Pattern.compile("Test email #1"),
                "admin@example.com",
                Pattern.compile("This is test email #1 sent from Jenkins"));

        /*
         * Navigate back to the dashboard first to dismiss the alert so that CspRule can check for violations (see
         * FormValidationTest).
         */
        jenkins.runThenConfirmAlert(() -> driver.findElement(By.xpath("//ol[@id=\"breadcrumbs\"]/li[1]/a"))
                .click());
        sleep(1000);
    }

    @Test
    public void send_mail_for_failed_build() throws IOException {
        FreeStyleJob job = jenkins.jobs.create();
        job.configure();
        job.addShellStep("fail");
        Mailer m = job.addPublisher(Mailer.class);
        m.recipients.set("dev@example.com mngmnt@example.com");
        job.save();

        job.startBuild().shouldFail();
        mailhog.assertMail(
                Pattern.compile("Build failed in Jenkins: .* #1"),
                "dev@example.com mngmnt@example.com",
                Pattern.compile("failure"));
    }
}
