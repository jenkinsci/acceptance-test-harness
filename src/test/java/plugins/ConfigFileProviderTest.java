package plugins;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.config_file_provider.ConfigFileProvider;
import org.jenkinsci.test.acceptance.plugins.config_file_provider.MavenSettingsConfig;
import org.jenkinsci.test.acceptance.plugins.config_file_provider.ServerCredentialMapping;
import org.jenkinsci.test.acceptance.plugins.credentials.CredentialsPage;
import org.jenkinsci.test.acceptance.plugins.credentials.ManagedCredentials;
import org.jenkinsci.test.acceptance.plugins.credentials.UserPwdCredential;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.WorkflowJob;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

/**
 * Tests config-file-provider plugin inside a Pipeline. 2.15.5 includes a major refactor as well as some bug fixes
 */
@WithPlugins({"config-file-provider@2.15.5", "workflow-job", "workflow-cps", "workflow-basic-steps", "workflow-durable-task-step"})
public class ConfigFileProviderTest extends AbstractJUnitTest {

    private static final String CRED_ID = "credId";
    private static final String CRED_USR = "fakeUser";
    private static final String CRED_PWD = "fakePass";
    private static final String SERVER_ID = "fakeServer";

    @Before
    public void setup() {
        CredentialsPage cp = new CredentialsPage(jenkins, ManagedCredentials.DEFAULT_DOMAIN);
        cp.open();

        UserPwdCredential cred = cp.add(UserPwdCredential.class);
        cred.username.set(CRED_USR);
        cred.password.set(CRED_PWD);
        cred.setId(CRED_ID);

        cp.create();
    }

    @Test
    public void testMavenSettingsConfigFile() {
        final MavenSettingsConfig mvnConfig = this.createMavenSettingsConfig(SERVER_ID, CRED_ID);
        final String jobLog = this.createPipelineAndGetConsole(mvnConfig);

        assertThat(jobLog, containsString(SERVER_ID));
        assertThat(jobLog, containsString(CRED_USR));
        assertThat(jobLog, containsString(CRED_PWD));
    }


    private MavenSettingsConfig createMavenSettingsConfig(final String serverId, final String credId) {
        final MavenSettingsConfig mvnConfig = new ConfigFileProvider(jenkins).addFile(MavenSettingsConfig.class);
        mvnConfig.open();

        mvnConfig.content(String.format("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<settings xmlns=\"http://maven.apache.org/SETTINGS/1.0.0\" \n" +
                "          xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n" +
                "          xsi:schemaLocation=\"http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd\">\n" +
                "  \n" +
                "  <servers>\n" +
                "    <server>\n" +
                "      <id>%s</id>\n" +
                "    </server>\n" +
                "  </servers>\n" +
                "\n" +
                "</settings>", serverId));

        final ServerCredentialMapping serverCred = mvnConfig.addServerCredentialMapping();
        serverCred.serverId(serverId);
        serverCred.credentialsId(credId);

        mvnConfig.save();
        return mvnConfig;
    }

    private String createPipelineAndGetConsole(final MavenSettingsConfig mvnConfig) {
        final WorkflowJob job = jenkins.jobs.create(WorkflowJob.class);
        job.script.set(String.format("node {\n" +
                "    configFileProvider(\n" +
                "        [configFile(fileId: '%s', variable: 'MAVEN_SETTINGS')]) {\n" +
                "            \n" +
                "        sh 'cat $MAVEN_SETTINGS '\n" +
                "    }\n" +
                "}", mvnConfig.id()));

        job.save();

        final Build b = job.startBuild().shouldSucceed();
        return b.getConsole();
    }

}