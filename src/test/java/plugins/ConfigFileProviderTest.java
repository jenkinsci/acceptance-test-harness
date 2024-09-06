package plugins;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

import org.apache.commons.lang3.SystemUtils;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.config_file_provider.ConfigFileProvider;
import org.jenkinsci.test.acceptance.plugins.config_file_provider.CustomConfig;
import org.jenkinsci.test.acceptance.plugins.config_file_provider.MavenSettingsConfig;
import org.jenkinsci.test.acceptance.plugins.config_file_provider.ServerCredentialMapping;
import org.jenkinsci.test.acceptance.plugins.credentials.CredentialsPage;
import org.jenkinsci.test.acceptance.plugins.credentials.ManagedCredentials;
import org.jenkinsci.test.acceptance.plugins.credentials.UserPwdCredential;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.Folder;
import org.jenkinsci.test.acceptance.po.WorkflowJob;
import org.jenkinsci.test.acceptance.utils.PipelineTestUtils;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests config-file-provider plugin inside a Pipeline.
 */
@WithPlugins({
    "config-file-provider",
    "workflow-job",
    "workflow-cps",
    "workflow-basic-steps",
    "workflow-durable-task-step",
    "cloudbees-folder"
})
public class ConfigFileProviderTest extends AbstractJUnitTest {

    private static final String CRED_ID = "credId";
    private static final String CRED_USR = "fakeUser";
    private static final String CRED_PWD = "fakePass";
    private static final String SERVER_ID = "fakeServer";
    private static final String CUSTOM_CONF_CONTENT = "test_custom_content for custom file";

    private static final String ANOTHER_SERVER_ID = "anotherServer";
    private static final String CUSTOM_CONF_EXTRA_CONTENT = "extra content for custom";

    private static final String MANAGED_FILE_NOT_FOUND_ERROR = "not able to provide the file [ManagedFile: id=%s";

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
    }

    private MavenSettingsConfig createMavenSettingsConfig(final String serverId, final String credId) {
        final MavenSettingsConfig mvnConfig = new ConfigFileProvider(jenkins).addFile(MavenSettingsConfig.class);

        mvnConfig.content(String.format(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                        + "<settings xmlns=\"http://maven.apache.org/SETTINGS/1.0.0\" \n"
                        + "          xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n"
                        + "          xsi:schemaLocation=\"http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd\">\n"
                        + "  \n"
                        + "  <servers>\n"
                        + "    <server>\n"
                        + "      <id>%s</id>\n"
                        + "    </server>\n"
                        + "  </servers>\n"
                        + "\n"
                        + "</settings>",
                serverId));

        final ServerCredentialMapping serverCred = mvnConfig.addServerCredentialMapping();
        serverCred.serverId(serverId);
        serverCred.credentialsId(credId);

        mvnConfig.save();
        return mvnConfig;
    }

    private String createPipelineAndGetConsole(final MavenSettingsConfig mvnConfig) {
        final WorkflowJob job = jenkins.jobs.create(WorkflowJob.class);
        job.script.set(String.format(
                "node {\n" + "    configFileProvider(\n"
                        + "        [configFile(fileId: '%s', variable: 'MAVEN_SETTINGS')]) {\n"
                        + "            \n"
                        + (SystemUtils.IS_OS_WINDOWS
                                ? "        bat 'type %%MAVEN_SETTINGS%% '\n"
                                : "        sh 'cat $MAVEN_SETTINGS '\n")
                        + "    }\n"
                        + "}",
                mvnConfig.id()));

        job.save();

        final Build b = job.startBuild().shouldSucceed();
        return b.getConsole();
    }

    @Test
    public void testCustomConfigFile() {
        final CustomConfig customConfig = this.createCustomConfig();
        final String jobLog = this.createPipelineAndGetConsole(customConfig);

        assertThat(jobLog, containsString(CUSTOM_CONF_CONTENT));
    }

    private CustomConfig createCustomConfig() {
        final CustomConfig customConfig = new ConfigFileProvider(jenkins).addFile(CustomConfig.class);

        customConfig.content(CUSTOM_CONF_CONTENT);

        customConfig.save();
        return customConfig;
    }

    private String createPipelineAndGetConsole(final CustomConfig customConfig) {
        final WorkflowJob job = jenkins.jobs.create(WorkflowJob.class);
        job.script.set(String.format(
                "node {\n" + "    configFileProvider(\n"
                        + "        [configFile(fileId: '%s', variable: 'CUSTOM_SETTINGS')]) {\n"
                        + "            \n"
                        + (SystemUtils.IS_OS_WINDOWS
                                ? "        bat 'type %%CUSTOM_SETTINGS%% '\n"
                                : "        sh 'cat $CUSTOM_SETTINGS '\n")
                        + "    }\n"
                        + "}",
                customConfig.id()));

        job.save();

        final Build b = job.startBuild().shouldSucceed();
        return b.getConsole();
    }

    @Test
    public void testConfigFileProviderWithFolders() {
        final MavenSettingsConfig mvnConfig = this.createMavenSettingsConfig(SERVER_ID, ANOTHER_SERVER_ID, CRED_ID);

        final Folder f = jenkins.jobs.create(Folder.class);
        final CustomConfig customConf = this.createCustomConfigInFolder(f, CUSTOM_CONF_CONTENT);
        final WorkflowJob job = createPipelineJobInFolderWithScript(
                f, scriptForPipelineWithParameters(mvnConfig.id(), customConf.id()));

        String jobLog = this.buildJobAndGetConsole(job, true);

        assertThat(jobLog, containsString(SERVER_ID));
        assertThat(jobLog, containsString(CRED_USR));
        assertThat(jobLog, containsString(ANOTHER_SERVER_ID));
        assertThat(jobLog, containsString(CUSTOM_CONF_CONTENT));

        this.makeChangesInConfigurations(mvnConfig, customConf);
        jobLog = this.buildJobAndGetConsole(job, true);

        assertThat(jobLog, containsString(SERVER_ID));
        assertThat(jobLog, containsString(CRED_USR));
        assertThat(jobLog, not(containsString(ANOTHER_SERVER_ID)));
        assertThat(jobLog, containsString(CUSTOM_CONF_CONTENT + CUSTOM_CONF_EXTRA_CONTENT));

        // We want to delete the config file and re-run the job to see it fail
        jenkins.visit("configfiles");
        runThenHandleDialog(() -> {
            driver.findElement(
                            by.xpath("//td[.='%s']/parent::tr/td[2]/a[1]", mvnConfig.id()) // this won't age well
                            )
                    .click();
        });

        jobLog = this.buildJobAndGetConsole(job, false);

        assertThat(jobLog, containsString(String.format(MANAGED_FILE_NOT_FOUND_ERROR, mvnConfig.id())));
    }

    private MavenSettingsConfig createMavenSettingsConfig(
            final String serverWithCreds, final String serverWithoutCreds, final String credId) {
        final MavenSettingsConfig mvnConfig = new ConfigFileProvider(jenkins).addFile(MavenSettingsConfig.class);

        mvnConfig.replaceAll(false);
        mvnConfig.content(String.format(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                        + "<settings xmlns=\"http://maven.apache.org/SETTINGS/1.0.0\" \n"
                        + "          xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n"
                        + "          xsi:schemaLocation=\"http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd\">\n"
                        + "  \n"
                        + "  <servers>\n"
                        + "    <server>\n"
                        + "      <id>%s</id>\n"
                        + "    </server>\n"
                        + "    <server>\n"
                        + "      <id>%s</id>\n"
                        + "    </server>\n"
                        + "  </servers>\n"
                        + "\n"
                        + "</settings>",
                serverWithCreds, serverWithoutCreds));

        final ServerCredentialMapping serverCred = mvnConfig.addServerCredentialMapping();
        serverCred.serverId(serverWithCreds);
        serverCred.credentialsId(credId);

        mvnConfig.save();
        return mvnConfig;
    }

    private CustomConfig createCustomConfigInFolder(final Folder f, final String content) {
        final CustomConfig customConf = new ConfigFileProvider(f).addFile(CustomConfig.class);

        customConf.content(content);

        customConf.save();
        return customConf;
    }

    private String buildJobAndGetConsole(final WorkflowJob job, final boolean expectedSuccess) {
        final Build b = job.startBuild();

        if (expectedSuccess) {
            b.shouldSucceed();
        } else {
            b.shouldFail();
        }

        return b.getConsole();
    }

    private void makeChangesInConfigurations(final MavenSettingsConfig mvnConfig, final CustomConfig customConf) {
        mvnConfig.open();
        mvnConfig.replaceAll(true);
        mvnConfig.save();

        customConf.open();
        customConf.content(CUSTOM_CONF_CONTENT + CUSTOM_CONF_EXTRA_CONTENT);
        customConf.save();
    }

    public String scriptForPipeline() {
        if (SystemUtils.IS_OS_UNIX) {
            return "node {\n" + "    configFileProvider(\n"
                    + "        [configFile(fileId: '%s', variable: 'MAVEN_SETTINGS'),\n"
                    + "         configFile(fileId: '%s', variable: 'CUSTOM_SETTINGS')]) {\n"
                    + "        sh 'cat $MAVEN_SETTINGS '\n"
                    + "        sh 'cat $CUSTOM_SETTINGS '\n"
                    + "    }\n"
                    + "}";
        } else {
            return "node {\n" + "    configFileProvider(\n"
                    + "        [configFile(fileId: '%s', variable: 'MAVEN_SETTINGS'),\n"
                    + "         configFile(fileId: '%s', variable: 'CUSTOM_SETTINGS')]) {\n"
                    + "        bat '@type %%MAVEN_SETTINGS%% '\n"
                    + "        bat '@type %%CUSTOM_SETTINGS%% '\n"
                    + "    }\n"
                    + "}";
        }
    }

    public WorkflowJob createPipelineJobInFolderWithScript(final Folder f, final String script) {
        return PipelineTestUtils.createPipelineJobWithScript(f.getJobs(), script);
    }

    public String scriptForPipelineWithParameters(final String... scriptParameters) {
        final String script = this.scriptForPipeline();
        PipelineTestUtils.checkScript(script);

        return String.format(script, (Object[]) scriptParameters);
    }
}
