package plugins;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.credentials.CredentialsPage;
import org.jenkinsci.test.acceptance.plugins.credentials.ManagedCredentials;
import org.jenkinsci.test.acceptance.plugins.credentials.StringCredentials;
import org.jenkinsci.test.acceptance.plugins.credentialsbinding.ManagedCredentialsBinding;
import org.jenkinsci.test.acceptance.plugins.credentialsbinding.SecretStringCredentialsBinding;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.ShellBuildStep;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

@WithPlugins ({"plain-credentials", "credentials-binding", "credentials@2.0"})
public class CredentialsBindingTest extends AbstractJUnitTest {

    private static final String SECRET_TEXT = "secret";
    private static final String SECRET_OUTPUT = " variable binded";


    @Test
    public void testTextBinding() {
        CredentialsPage mc = new CredentialsPage(jenkins, ManagedCredentials.DEFAULT_DOMAIN);
        mc.open();
        StringCredentials cred = mc.add(StringCredentials.class);
        cred.scope.select("GLOBAL");
        cred.secret.set(SECRET_TEXT);
        mc.create();

        FreeStyleJob job = jenkins.jobs.create();
        job.configure();
        job.check("Use secret text(s) or file(s)");
        ManagedCredentialsBinding mcb = new ManagedCredentialsBinding(job);
        SecretStringCredentialsBinding cb = mcb.addCredentialBinding(SecretStringCredentialsBinding.class);
        cb.variable.set("BINDED_SECRET");
        ShellBuildStep shell = job.addBuildStep(ShellBuildStep.class);
        shell.command("if [ \"$BINDED_SECRET\" = \"" + SECRET_TEXT + "\" ] \n then \n echo \"" + SECRET_OUTPUT + "\" \n fi");
        job.save();
        
        Build b = job.scheduleBuild();
        b.shouldSucceed();
        assertThat(b.getConsole(), containsString(SECRET_OUTPUT));
    }
}
