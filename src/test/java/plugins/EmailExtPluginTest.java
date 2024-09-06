package plugins;

import com.google.inject.Inject;
import java.util.regex.Pattern;
import org.jenkinsci.test.acceptance.docker.fixtures.MailhogContainer;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.DockerTest;
import org.jenkinsci.test.acceptance.junit.WithDocker;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.email_ext.EmailExtPublisher;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.utils.mail.MailhogProvider;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@WithPlugins("email-ext")
@Category(DockerTest.class)
@WithDocker
public class EmailExtPluginTest extends AbstractJUnitTest {

    @Inject
    MailhogProvider mailhogProvider;

    @Test
    public void build() {
        MailhogContainer mailhog = mailhogProvider.get();

        FreeStyleJob job = jenkins.jobs.create();
        job.configure();
        job.addShellStep("false");
        EmailExtPublisher pub = job.addPublisher(EmailExtPublisher.class);
        pub.subject.set("Modified $DEFAULT_SUBJECT");
        pub.setRecipient("dev@example.com");
        pub.body.set("$DEFAULT_CONTENT\nwith amendment");
        job.save();

        Build b = job.startBuild().shouldFail();

        mailhog.assertMail(Pattern.compile("^Modified "), "dev@example.com", Pattern.compile("\nwith amendment$"));
    }
}
