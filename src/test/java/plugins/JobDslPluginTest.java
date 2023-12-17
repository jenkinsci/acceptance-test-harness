package plugins;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.jenkinsci.test.acceptance.Matchers.*;
import static org.jenkinsci.test.acceptance.po.View.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.authorize_project.ProjectDefaultBuildAccessControl;
import org.jenkinsci.test.acceptance.plugins.config_file_provider.ConfigFileProvider;
import org.jenkinsci.test.acceptance.plugins.job_dsl.JobDslBuildStep;
import org.jenkinsci.test.acceptance.plugins.job_dsl.JobDslLookupStrategy;
import org.jenkinsci.test.acceptance.plugins.job_dsl.JobDslRemovedConfigFilesAction;
import org.jenkinsci.test.acceptance.plugins.job_dsl.JobDslRemovedJobAction;
import org.jenkinsci.test.acceptance.plugins.job_dsl.JobDslRemovedViewAction;
import org.jenkinsci.test.acceptance.plugins.matrix_auth.MatrixAuthorizationStrategy;
import org.jenkinsci.test.acceptance.plugins.matrix_auth.MatrixRow;
import org.jenkinsci.test.acceptance.plugins.mock_security_realm.MockSecurityRealm;
import org.jenkinsci.test.acceptance.plugins.script_security.ScriptApproval;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.ContainerPageObject;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Folder;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.GlobalSecurityConfig;
import org.jenkinsci.test.acceptance.po.Job;
import org.jenkinsci.test.acceptance.po.ListView;
import org.jenkinsci.test.acceptance.po.PluginManager;
import org.jenkinsci.test.acceptance.po.View;
import org.jenkinsci.test.acceptance.update_center.PluginSpec;
import org.jenkinsci.test.acceptance.utils.IOUtil;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * Acceptance tests for the Job DSL plugin.
 *
 * @author Maximilian Oeckler, Kevin Beck, Manuel Reinbold
 */
@WithPlugins("job-dsl")
public class JobDslPluginTest extends AbstractJUnitTest {

    private static final String ADMIN = "admin";
    private static final String USER = "user";

    private static final String LIST_VIEW_NAME = "testListView";
    private static final String EXAMPLE_ENABLED_NAME = "example-enabled";
    private static final String EXAMPLE_DISABLED_NAME = "example-disabled";
    private static final String LIST_VIEW_REGEX = ".*abled.*";

    /**
     * Verifies that all configurations, done on the job configuration page,
     * are saved correctly.
     */
    @Test @WithPlugins({"matrix-auth@2.3","mock-security-realm"})
    public void should_save_configurations() {
        FreeStyleJob seedJob = createSeedJob();
        JobDslBuildStep jobDsl = seedJob.addBuildStep(JobDslBuildStep.class);
        jobDsl.setScriptTargetsOnFilesystem("JobTest.groovy", "AnotherJobTest.groovy");
        jobDsl.setIgnoreMissingFiles(true);
        jobDsl.setIgnoreExisting(true);
        jobDsl.setRemovedJobAction(JobDslRemovedJobAction.DELETE);
        jobDsl.setRemovedViewAction(JobDslRemovedViewAction.DELETE);
        jobDsl.setRemovedConfigFilesAction(JobDslRemovedConfigFilesAction.DELETE);
        jobDsl.setLookupStrategy(JobDslLookupStrategy.SEED_JOB);
        jobDsl.setAdditionalClasspath("path1", "path2");
        jobDsl.setFailOnMissingPlugin(true);
        jobDsl.setUnstableOnDeprecation(true);
        seedJob.save();

        seedJob.configure();
        assertThat(jobDsl.isLookOnFilesystem(), is(true));
        assertThat(jobDsl.getScriptTargetsOnFilesystem(), equalTo(new String[]{"JobTest.groovy", "AnotherJobTest.groovy"}));
        assertThat(jobDsl.isIgnoreMissingFiles(), is(true));
        assertThat(jobDsl.isIgnoreExisting(), is(true));
        assertThat(jobDsl.getRemovedJobAction(), equalTo(JobDslRemovedJobAction.DELETE));
        assertThat(jobDsl.getRemovedViewAction(), equalTo(JobDslRemovedViewAction.DELETE));
        assertThat(jobDsl.getRemovedConfigFilesAction(), equalTo(JobDslRemovedConfigFilesAction.DELETE));
        assertThat(jobDsl.getLookupStrategy(), equalTo(JobDslLookupStrategy.SEED_JOB));
        assertThat(jobDsl.getAdditionalClasspath(), equalTo(new String[]{"path1", "path2"}));
        assertThat(jobDsl.isFailOnMissingPlugin(), is(true));
        assertThat(jobDsl.isUnstableOnDeprecation(), is(true));

        String script = "job('JobDSL-Test') {\n" +
                        "    description('This is a description.')\n" +
                        "}";
        jobDsl.setScript(script);
        seedJob.save();

        seedJob.configure();
        assertThat(jobDsl.isUseScriptText(), is(true));
        assertThat(jobDsl.getScript(), equalTo(script));

        setUpSecurity();
        jenkins.login().doLogin(ADMIN);

        seedJob.configure(() -> jobDsl.setUseSandbox(true));
        seedJob.configure();
        assertThat(jobDsl.isUseSandbox(), is(true));
    }

    /**
     * Verifies that a list of newline separated DSL scripts, located in the workspace,
     * is read an executed in the same order as specified.
     * Therefore the first script creates an folder. The second script creates a job
     * in this folder. If the order is changed, the build will fail because the
     * folder was not created yet.
     */
    @Test @WithPlugins("cloudbees-folder")
    public void should_look_on_filesystem() {
        FreeStyleJob seedJob = createSeedJob();
        seedJob.copyResource(resource("/job_dsl_plugin/CreateFolder.groovy"));
        seedJob.copyResource(resource("/job_dsl_plugin/CreateJobInFolder.groovy"));
        JobDslBuildStep jobDsl = seedJob.addBuildStep(JobDslBuildStep.class);
        jobDsl.setScriptTargetsOnFilesystem("CreateFolder.groovy", "CreateJobInFolder.groovy");
        seedJob.save();
        Build build = seedJob.scheduleBuild().shouldSucceed();
        Pattern expected = Pattern.compile("Processing DSL script CreateFolder.groovy(\\s*)" +
                                           "Processing DSL script CreateJobInFolder.groovy");
        assertThat(build.getConsole(), containsRegexp(expected));
    }

    /**
     * Verifies whether missing files will be ignored or fail the build step.
     * If 'Ignore missing files' is checked, missing files are ignored.
     * Otherwise the build step will fail.
     */
    @Test
    public void should_ignore_missing_files() {
        FreeStyleJob seedJob = createSeedJob();
        JobDslBuildStep jobDsl = seedJob.addBuildStep(JobDslBuildStep.class);
        jobDsl.setScriptTargetsOnFilesystem("CreateFolder.groovy");
        seedJob.save();
        seedJob.scheduleBuild().shouldFail();
        seedJob.configure(() -> jobDsl.setIgnoreMissingFiles(true));
        seedJob.scheduleBuild().shouldSucceed();
    }

    /**
     * Verifies whether previously generated jobs will be ignored or updated.
     * By default, all previous generated jobs will be updated.
     * If existing jobs should be ignored, the plugin do not modify previous
     * generated jobs.
     */
    @Test
    public void should_ignore_existing_jobs() {
        FreeStyleJob seedJob = createSeedJob();
        JobDslBuildStep jobDsl = seedJob.addBuildStep(JobDslBuildStep.class);
        jobDsl.setScript("job('Existing_Job') {\n" +
                         "   description('Existing description');\n" +
                         "}");
        seedJob.save();
        seedJob.scheduleBuild().shouldSucceed();
        FreeStyleJob existingJob = getJob("Existing_Job");

        seedJob.configure(() -> {
            jobDsl.setScript("job('Existing_Job') {\n" +
                             "   description('This is a description');\n" +
                             "}");
            jobDsl.setIgnoreExisting(true);
        });
        Build build = seedJob.scheduleBuild().shouldSucceed();

        Pattern expected = Pattern.compile("Existing items:(\\s*)GeneratedJob[{]name='Existing_Job'}");
        assertThat(build.getConsole(), containsRegexp(expected));
        checkDescription(existingJob, "Existing description");

        seedJob.configure(() -> jobDsl.setIgnoreExisting(false));
        Build build2 = seedJob.scheduleBuild().shouldSucceed();
        assertThat(build2.getConsole(), containsRegexp(expected));
        checkDescription(existingJob, "This is a description");
    }

    /**
     * Verifies whether previously generated views will be ignored or updated.
     * By default, all previous generated views will be updated.
     * If existing views should be ignored, the plugin do not modify previous
     * generated views.
     */
    @Test
    public void should_ignore_existing_views() {
        FreeStyleJob seedJob = createSeedJob();
        JobDslBuildStep jobDsl = seedJob.addBuildStep(JobDslBuildStep.class);
        jobDsl.setScript("listView('Existing_View') {\n" +
                         "   description('Existing description');\n" +
                         "}");
        seedJob.save();
        seedJob.scheduleBuild().shouldSucceed();
        ListView existingView = getView("Existing_View");

        seedJob.configure(() -> {
            jobDsl.setScript("listView('Existing_View') {\n" +
                    "   description('This is a description');\n" +
                    "}");
            jobDsl.setIgnoreExisting(true);
        });
        Build build = seedJob.scheduleBuild().shouldSucceed();

        Pattern expected = Pattern.compile("Existing views:(\\s*)GeneratedView[{]name='Existing_View'}");
        assertThat(build.getConsole(), containsRegexp(expected));
        checkDescription(existingView, "Existing description");

        seedJob.configure(() -> jobDsl.setIgnoreExisting(false));
        Build build2 = seedJob.scheduleBuild().shouldSucceed();
        assertThat(build2.getConsole(), containsRegexp(expected));
        checkDescription(existingView, "This is a description");
    }

    /**
     * Verifies whether a previously generated job will be ignored if it
     * is not referenced anymore.
     */
    @Test
    public void should_ignore_removed_jobs() {
        FreeStyleJob seedJob = executeRemovedJobAction(JobDslRemovedJobAction.IGNORE);
        Build build = seedJob.scheduleBuild().shouldSucceed();
        Pattern expected = Pattern.compile("Unreferenced items:(\\s*)GeneratedJob[{]name='Old_Job'}");
        assertThat(build.getConsole(), containsRegexp(expected));

        FreeStyleJob oldJob = getJob("Old_Job");
        oldJob.open();
        assertThat(driver, not(Job.disabled()));
    }

    /**
     * Verifies whether a previously generated job will be disabled if it
     * is not referenced anymore.
     */
    @Test
    public void should_disable_removed_jobs() {
        FreeStyleJob seedJob = executeRemovedJobAction(JobDslRemovedJobAction.DISABLE);
        Build build = seedJob.scheduleBuild().shouldSucceed();
        Pattern expected = Pattern.compile("Unreferenced items:(\\s*)GeneratedJob[{]name='Old_Job'}(\\s*)Disabled items:(\\s*)GeneratedJob[{]name='Old_Job'}");
        assertThat(build.getConsole(), containsRegexp(expected));

        FreeStyleJob oldJob = getJob("Old_Job");
        oldJob.open();
        assertThat(driver, Job.disabled());
    }

    /**
     * Verifies whether a previously generated job will be deleted if it
     * is not referenced anymore.
     */
    @Test
    public void should_delete_removed_jobs() {
        FreeStyleJob seedJob = executeRemovedJobAction(JobDslRemovedJobAction.DELETE);
        Build build = seedJob.scheduleBuild().shouldSucceed();
        Pattern expected = Pattern.compile("Unreferenced items:(\\s*)GeneratedJob[{]name='Old_Job'}(\\s*)Removed items:(\\s*)GeneratedJob[{]name='Old_Job'}");
        assertThat(build.getConsole(), containsRegexp(expected));

        FreeStyleJob oldJob = getJob("Old_Job");
        assertThat(oldJob, pageObjectDoesNotExist());
    }

    /**
     * Let a seed job run a Job DSL script that do not reference a previous generated job anymore.
     * @param action The action what to do when a previously generated job is not referenced anymore.
     * @return The executed seed job.
     */
    private FreeStyleJob executeRemovedJobAction(JobDslRemovedJobAction action) {
        FreeStyleJob seedJob = createSeedJob();
        JobDslBuildStep jobDsl = seedJob.addBuildStep(JobDslBuildStep.class);
        jobDsl.setScript("job('Old_Job')");
        seedJob.save();
        seedJob.scheduleBuild().shouldSucceed();

        seedJob.configure(() -> {
            jobDsl.setScript("job('New_Job')");
            jobDsl.setRemovedJobAction(action);
        });
        return seedJob;
    }

    /**
     * Verifies whether a previously generated view will be ignored if it
     * is not referenced anymore.
     */
    @Test
    public void should_ignore_removed_views() {
        FreeStyleJob seedJob = executeRemovedViewAction(JobDslRemovedViewAction.IGNORE);
        Build build = seedJob.scheduleBuild().shouldSucceed();
        Pattern expected = Pattern.compile("Unreferenced views:(\\s*)GeneratedView[{]name='Old_View'}");
        assertThat(build.getConsole(), containsRegexp(expected));

        ListView oldView = getView("Old_View");
        assertThat(oldView, pageObjectExists());
    }

    /**
     * Verifies whether a previously generated view will be deleted if it
     * is not referenced anymore.
     */
    @Test
    public void should_delete_removed_views() {
        FreeStyleJob seedJob = executeRemovedViewAction(JobDslRemovedViewAction.DELETE);
        Build build = seedJob.scheduleBuild().shouldSucceed();
        Pattern expected = Pattern.compile("Unreferenced views:(\\s*)GeneratedView[{]name='Old_View'}(\\s*)Removed views:(\\s*)GeneratedView[{]name='Old_View'}");
        assertThat(build.getConsole(), containsRegexp(expected));

        ListView oldView = getView("Old_View");
        assertThat(oldView, pageObjectDoesNotExist());
    }

    /**
     * Let a seed job run a Job DSL script that do not reference a previous generated view anymore.
     * @param action The action what to do when a previously generated view is not referenced anymore.
     * @return The executed seed job.
     */
    private FreeStyleJob executeRemovedViewAction(JobDslRemovedViewAction action) {
        FreeStyleJob seedJob = createSeedJob();
        JobDslBuildStep jobDsl = seedJob.addBuildStep(JobDslBuildStep.class);
        jobDsl.setScript("listView('Old_View')");
        seedJob.save();
        seedJob.scheduleBuild().shouldSucceed();

        seedJob.configure(() -> {
            jobDsl.setScript("listView('New_View')");
            jobDsl.setRemovedViewAction(action);
        });
        return seedJob;
    }

    /**
     * Verifies whether a previously generated config file will be ignored if it
     * is not referenced anymore.
     */
    @Test @WithPlugins("config-file-provider")
    public void should_ignore_removed_config_files() {
        FreeStyleJob seedJob = executeRemovedConfigFilesAction(JobDslRemovedConfigFilesAction.IGNORE);
        Build build = seedJob.scheduleBuild().shouldSucceed();
        Pattern expected = Pattern.compile("Unreferenced config files:(\\s*)GeneratedConfigFile[{]name='Old_Config_File', id='123456789'}");
        assertThat(build.getConsole(), containsRegexp(expected));

        ConfigFileProvider configFileProvider = new ConfigFileProvider(jenkins);
        configFileProvider.open();

        assertThat(driver, hasElement(by.xpath("//a[@href='editConfig?id=123456789']")));
    }

    /**
     * Verifies whether a previously generated config file will be deleted if it
     * is not referenced anymore.
     */
    @Test @WithPlugins("config-file-provider")
    public void should_delete_removed_config_files() {
        FreeStyleJob seedJob = executeRemovedConfigFilesAction(JobDslRemovedConfigFilesAction.DELETE);
        Build build = seedJob.scheduleBuild().shouldSucceed();
        Pattern expected = Pattern.compile("Unreferenced config files:(\\s*)GeneratedConfigFile[{]name='Old_Config_File', id='123456789'}");
        assertThat(build.getConsole(), containsRegexp(expected));

        ConfigFileProvider configFileProvider = new ConfigFileProvider(jenkins);
        configFileProvider.open();

        assertThat(driver, not(hasElement(by.xpath("//a[@href='editConfig?id=123456789']"))));
    }

    /**
     * Let a seed job run a Job DSL script that do not reference a previous generated config file anymore.
     * @param action The action what to do when a previously generated config file is not referenced anymore.
     * @return The executed seed job.
     */
    private FreeStyleJob executeRemovedConfigFilesAction(JobDslRemovedConfigFilesAction action) {
        FreeStyleJob seedJob = createSeedJob();
        JobDslBuildStep jobDsl = seedJob.addBuildStep(JobDslBuildStep.class);
        jobDsl.setScript("configFiles {\n" +
                "  customConfig {\n" +
                "    id('123456789')\n" +
                "    name('Old_Config_File')\n" +
                "    comment('This is a comment.')\n" +
                "    content('My Content')\n" +
                "    providerId('')\n" +
                "  }\n" +
                "}");
        seedJob.save();
        seedJob.scheduleBuild().shouldSucceed();

        seedJob.configure(() -> {
            jobDsl.setScript("configFiles {\n" +
                    "  customConfig {\n" +
                    "    id('456789123')\n" +
                    "    name('New_Config_File')\n" +
                    "    comment('This is a comment.')\n" +
                    "    content('My Content')\n" +
                    "    providerId('')\n" +
                    "  }\n" +
                    "}");
            jobDsl.setRemovedConfigFilesAction(action);
        });
        return seedJob;
    }

    /**
     * Verifies that relative job names are interpreted relative to the Jenkins root.
     * Even if the seed job is located in a folder, a new generated job is relative
     * to the Jenkins root.
     */
    @Test @WithPlugins("cloudbees-folder")
    public void should_lookup_at_jenkins_root() {
        Folder folder = runSeedJobInFolder("New_Job", JobDslLookupStrategy.JENKINS_ROOT);
        FreeStyleJob newJob = getJob("New_Job");
        assertThat(newJob, pageObjectExists());
        newJob = folder.getJobs().get(FreeStyleJob.class, "New_Job");
        assertThat(newJob, pageObjectDoesNotExist());
    }

    /**
     * Verifies that relative job names are interpreted relative to the folder in which
     * the seed job is located.
     */
    @Test @WithPlugins("cloudbees-folder")
    public void should_lookup_at_seed_job() {
        Folder folder = runSeedJobInFolder("New_Job", JobDslLookupStrategy.SEED_JOB);
        FreeStyleJob newJob = getJob("New_Job");
        assertThat(newJob, pageObjectDoesNotExist());
        newJob = folder.getJobs().get(FreeStyleJob.class, "New_Job");
        assertThat(newJob, pageObjectExists());
    }

    /**
     * Creates and executes a seed job in a folder. The seed job generates a new job
     * with the specified lookup strategy.
     * @param jobName The name the job to generate.
     * @param strategy The context to use for relative job names.
     * @return The folder in which the seed job is located.
     */
    private Folder runSeedJobInFolder(String jobName, JobDslLookupStrategy strategy) {
        final Folder folder = jenkins.jobs.create(Folder.class, "Folder");
        FreeStyleJob seedJob = folder.getJobs().create(FreeStyleJob.class, "Seed");
        JobDslBuildStep jobDsl = seedJob.addBuildStep(JobDslBuildStep.class);
        jobDsl.setScript("job('" + jobName + "')");
        jobDsl.setLookupStrategy(strategy);
        seedJob.save();
        seedJob.scheduleBuild().shouldSucceed();
        return folder;
    }

    /**
     * Verifies whether the build will be marked as failed or unstable if a plugin must
     * be installed to support all features used in the DSL script.
     */
    @Test
    public void should_fail_on_missing_plugin() {
        // check if plugin is installed. if true, disable plugin
        PluginSpec pluginSpec = new PluginSpec("chucknorris");
        PluginManager pm = jenkins.getPluginManager();
        if (pm.isInstalled(pluginSpec)) {
            pm.enablePlugin(pluginSpec.getName(), false);
            jenkins.restart();
        }

        FreeStyleJob seedJob = createSeedJob();
        JobDslBuildStep jobDsl = seedJob.addBuildStep(JobDslBuildStep.class);
        jobDsl.setScript("job('New_Job') {\n" +
                         "   publishers {\n" +
                         "      chucknorris()\n" +
                         "   }\n" +
                         "}");
        seedJob.save();
        Build build = seedJob.scheduleBuild().shouldBeUnstable();
        assertThat(build.getConsole(), containsString("plugin 'chucknorris' needs to be installed"));

        seedJob.configure(() -> jobDsl.setFailOnMissingPlugin(true));
        build = seedJob.scheduleBuild().shouldFail();
        assertThat(build.getConsole(), containsString("plugin 'chucknorris' needs to be installed"));
    }

    /**
     * Verifies that a newline separated list of additional classpath entries will be considered.
     * If Groovy classes are located in subfolders in the workspace or in JAR files,
     * the additional classpath option is needed to import these classes.
     */
    @Test
    public void should_use_additional_classpath() {
        FreeStyleJob seedJob = createSeedJob();
        seedJob.copyResource(resource("/job_dsl_plugin/MyUtilities.groovy"), "src/utilities/MyUtilities.groovy");
        seedJob.copyResource(resource("/job_dsl_plugin/Utility.jar"));
        JobDslBuildStep jobDsl = seedJob.addBuildStep(JobDslBuildStep.class);
        jobDsl.setScript("import utilities.MyUtilities\n" +
                         "import utilities.MyUtilitiesFromJar\n" +
                         "\n" +
                         "def job_folder = job('job_src_folder')\n" +
                         "MyUtilities.addDescription(job_folder)\n" +
                         "\n" +
                         "def job_jar = job('job_jar_file')\n" +
                         "MyUtilitiesFromJar.addDescription(job_jar)");

        seedJob.save();
        Build build = seedJob.scheduleBuild().shouldFail();
        assertThat(build.getConsole(), containsString("unable to resolve class utilities.MyUtilities"));
        assertThat(build.getConsole(), containsString("unable to resolve class utilities.MyUtilitiesFromJar"));

        assertThat(getJob("job_src_folder"), pageObjectDoesNotExist());
        assertThat(getJob("job_jar_file"), pageObjectDoesNotExist());

        seedJob.configure(() -> jobDsl.setAdditionalClasspath("src", "Utility.jar"));

        Build build2 = seedJob.scheduleBuild().shouldSucceed();
        assertThat(build2.getConsole(), containsString("GeneratedJob{name='job_src_folder'}"));
        assertThat(build2.getConsole(), containsString("GeneratedJob{name='job_jar_file'}"));

        checkDescription(getJob("job_src_folder"), "Description from class in src folder.");
        checkDescription(getJob("job_jar_file"), "Description from class in JAR file.");
    }

    /**
     * Verifies that if script security for Job DSL scripts is enabled,
     * scripts saved by non administrators that not run in a Groovy sandbox
     * wont be executed, because they are not approved.
     * If script security for Job DSL scripts is disabled, the script can be executed.
     */
    @Test @WithPlugins({"matrix-auth@2.3","mock-security-realm"})
    public void should_use_script_security() {
        GlobalSecurityConfig sc = setUpSecurity();

        jenkins.login().doLogin(USER);
        FreeStyleJob seedJob = createSeedJob();
        JobDslBuildStep jobDsl = seedJob.addBuildStep(JobDslBuildStep.class);
        jobDsl.setScript("job('New_Job')");
        jobDsl.setUseSandbox(false);
        seedJob.save();

        // Build should fail because script is saved from non administrator an not yet approved
        Build build = seedJob.scheduleBuild().shouldFail();
        assertThat(build.getConsole(), containsString("script not yet approved for use"));

        jenkins.logout();
        jenkins.login().doLogin(ADMIN);

        // Build should fail because script is saved from non administrator an not yet approved
        Build build2 = seedJob.scheduleBuild().shouldFail();
        assertThat(build2.getConsole(), containsString("script not yet approved for use"));

        sc.configure(() -> sc.setJobDslScriptSecurity(false));

        jenkins.logout();
        jenkins.login().doLogin(USER);

        // Build should succeed because script is approved now
        seedJob.scheduleBuild().shouldSucceed();
    }

    /**
     * Verifies that if script security for Job DSL scripts is enabled and Jenkins
     * security is enabled, it is not possible to import Groovy classes from the
     * workspace.
     */
    @Test @WithPlugins({"matrix-auth@2.3","mock-security-realm"})
    public void should_disallow_importing_groovy_classes_when_script_security_enabled() {
        GlobalSecurityConfig sc = setUpSecurity();

        jenkins.login().doLogin(ADMIN);
        FreeStyleJob seedJob = createSeedJob();
        seedJob.copyResource(resource("/job_dsl_plugin/MyUtilities.groovy"), "utilities/MyUtilities.groovy");
        JobDslBuildStep jobDsl = seedJob.addBuildStep(JobDslBuildStep.class);
        jobDsl.setScript("import utilities.MyUtilities\n" +
                "\n" +
                "def newJob = job('New_Job')\n" +
                "MyUtilities.addDescription(newJob)");

        seedJob.save();

        // Build should fail because importing Groovy classes not allowed if script security is enabled
        Build build = seedJob.scheduleBuild().shouldFail();
        assertThat(build.getConsole(), containsString("ERROR: script not yet approved for use"));

        sc.configure(() -> sc.setJobDslScriptSecurity(false));

        seedJob.scheduleBuild().shouldSucceed();
    }

    /**
     * Verifies that if script security for Job DSL scripts is enabled,
     * scripts saved by non administrators that not run in a Groovy sandbox
     * wont be executed.
     * Administrators can approve scripts in the 'Script Approval' of the
     * 'Manage Jenkins' area. Approved scripts can be executed.
     */
    @Test @WithPlugins({"matrix-auth@2.3","mock-security-realm"})
    public void should_use_script_approval() {
        setUpSecurity();

        jenkins.login().doLogin(USER);
        FreeStyleJob seedJob = createSeedJob();
        JobDslBuildStep jobDsl = seedJob.addBuildStep(JobDslBuildStep.class);
        jobDsl.setScript("job('New_Job')");
        jobDsl.setUseSandbox(false);
        seedJob.save();

        // Build should fail because script is saved from non administrator an not yet approved
        Build build = seedJob.scheduleBuild().shouldFail();
        assertThat(build.getConsole(), containsString("script not yet approved for use"));

        jenkins.logout();
        jenkins.login().doLogin(ADMIN);

        // Build should fail because script is saved from non administrator an not yet approved
        Build build2 = seedJob.scheduleBuild().shouldFail();
        assertThat(build2.getConsole(), containsString("script not yet approved for use"));

        ScriptApproval sa = new ScriptApproval(jenkins);
        sa.open();
        sa.find(seedJob.name).approve();

        jenkins.logout();
        jenkins.login().doLogin(USER);

        // Build should succeed because script is approved now
        seedJob.scheduleBuild().shouldSucceed();
    }

    /**
     * Verifies that if script security for Job DSL scripts is enabled,
     * scripts saved by non administrators can run in a Groovy sandbox
     * without approval. All Job DSL methods are whitelisted by default.
     */
    @Test @WithPlugins({"matrix-auth@2.3","mock-security-realm","authorize-project"})
    public void should_use_grooy_sandbox_whitelisted_content() {
        GlobalSecurityConfig sc = setUpSecurity();
        runBuildAsUserWhoTriggered(sc);

        jenkins.login().doLogin(USER);
        FreeStyleJob seedJob = createSeedJob();
        JobDslBuildStep jobDsl = seedJob.addBuildStep(JobDslBuildStep.class);
        jobDsl.setScript("job('New_Job')");
        jobDsl.setUseSandbox(false);
        seedJob.save();

        // Build should fail because script is saved from non administrator an not yet approved
        Build build = seedJob.scheduleBuild().shouldFail();
        assertThat(build.getConsole(), containsString("script not yet approved for use"));

        seedJob.configure(() -> jobDsl.setUseSandbox(true));

        // Build should succeed because the script runs in Groovy sandbox
        // and only Job DSL methods are used.
        seedJob.scheduleBuild().shouldSucceed();
    }

    /**
     * Verifies that if script security for Job DSL scripts is enabled,
     * scripts with not whitelisted content saved by non administrators
     * wont be executed even it should run in a Groovy sandbox.
     * Administrators can approve this content in the 'Script Approval' of the
     * 'Manage Jenkins' area. Approved scripts can be executed.
     */
    @Test @WithPlugins({"matrix-auth@2.3","mock-security-realm","authorize-project"})
    @Ignore("https://github.com/jenkinsci/acceptance-test-harness/issues/1444")
    public void should_use_grooy_sandbox_no_whitelisted_content() {
        GlobalSecurityConfig sc = setUpSecurity();
        runBuildAsUserWhoTriggered(sc);

        jenkins.login().doLogin(USER);
        FreeStyleJob seedJob = createSeedJob();
        JobDslBuildStep jobDsl = seedJob.addBuildStep(JobDslBuildStep.class);
        jobDsl.setScript("def jobNames = [\"First_Job\", \"Second_Job\"].toArray()\n" +
                         "\n" +
                         "for(name in jobNames) {\n" +
                         "  job(name)\n" +
                         "}");
        jobDsl.setUseSandbox(true);
        seedJob.save();

        // Build should fail because script contains not whitelisted content.
        // It don't matter that the script runs in sandbox.
        Build build = seedJob.scheduleBuild().shouldFail();
        assertThat(build.getConsole(), containsString("Scripts not permitted to use method java.util.Collection toArray"));

        jenkins.logout();
        jenkins.login().doLogin(ADMIN);

        ScriptApproval sa = new ScriptApproval(jenkins);
        sa.open();
        sa.findSignature("toArray").approve();

        jenkins.logout();
        jenkins.login().doLogin(USER);

        // Build should succeed because the not whitelisted content was approved.
        seedJob.scheduleBuild().shouldSucceed();
    }

    /**
     * Verifies that Groovy sandbox can only used if 'Access Control for Builds'
     * is configured. The DSL job needs to run as a particular user.
     */
    @Test @WithPlugins({"matrix-auth@2.3","mock-security-realm","authorize-project"})
    public void should_run_grooy_sandbox_as_particular_user() {
        GlobalSecurityConfig sc = setUpSecurity();

        jenkins.login().doLogin(USER);
        FreeStyleJob seedJob = createSeedJob();
        JobDslBuildStep jobDsl = seedJob.addBuildStep(JobDslBuildStep.class);
        jobDsl.setScript("job('New_Job')");
        jobDsl.setUseSandbox(true);
        seedJob.save();

        // Build should fail because script runs in sandbox but no particular user is specified
        // which should run the build
        Build build = seedJob.scheduleBuild().shouldFail();
        assertThat(build.getConsole(), containsString("You must configure the DSL job to run as a specific user in order to use the Groovy sandbox"));

        runBuildAsUserWhoTriggered(sc);

        jenkins.login().doLogin(USER);
        // Build should succeed because now a particular user is specified
        seedJob.scheduleBuild().shouldSucceed();
    }

    /**
     * Verifies that the sidebar link 'Job DSL API Reference' on the project page of a job
     * is only shown if the job contains a Job DSL build step.
     * Further it checks that the API page could be shown if the link was clicked.
     */
    @Test
    public void should_show_job_dsl_api_reference() {
        String hrefLocator = "/plugin/job-dsl/api-viewer/index.html";

        FreeStyleJob seedJob = createSeedJob();
        seedJob.open();
        assertThat(driver, not(hasElement(by.href(hrefLocator))));

        seedJob.configure(() -> seedJob.addBuildStep(JobDslBuildStep.class));

        find(by.href(hrefLocator)).click();
        assertThat(driver, hasElement(by.link("Jenkins Job DSL Plugin")));

        seedJob.configure(seedJob::removeFirstBuildStep);
        assertThat(driver, not(hasElement(by.href(hrefLocator))));
    }

    /**
     * Verifies that an alert is shown on the project page of the generated job
     * if it has been changed manually since it was generated by the seed job.
     */
    @Test
    public void should_alert_if_generated_job_changed() {
        FreeStyleJob seedJob = createSeedJob();
        JobDslBuildStep jobDsl = seedJob.addBuildStep(JobDslBuildStep.class);
        jobDsl.setScript("job('New_Job')");
        seedJob.save();
        seedJob.scheduleBuild().shouldSucceed();

        FreeStyleJob generatedJob = getJob("New_Job");
        generatedJob.open();
        String alert = "This item has been changed manually since it was generated by the seed job.";
        assertThat(driver, not(hasContent(alert)));

        generatedJob.configure(() -> generatedJob.setDescription("New Description"));
        assertThat(driver, hasContent(alert));
    }

    @Test
    @WithPlugins({"sidebar-link", "build-name-setter", "envinject"})
    public void should_create_job() {

        // Arrange
        String jobName = "MyJob";
        String jobDescription = "My sample description";
        String jobDisplayName = "My job display name";
        String jobLabel = "!ubuntu";
        String linkUrl = "https://jenkins.io";
        String linkLabel = "jenkins.io";
        String jobDslScript = String.format(IOUtil.multiline(
                "job('%s') {",
                "    description('%s');",
                "    displayName('%s');",
                "    label('%s');",
                "    customWorkspace('/tmp/custom-workspace')",
                "    environmentVariables(varname: 'varval')",
                "    properties {",
                "        sidebarLinks {",
                "            link('%s', '%s')",
                "        }",
                "    };",
                "    wrappers { buildName('custom-name') }",
                "    concurrentBuild();",
                "    steps {",
                "        shell('sleep 10')",
                "    }",
                "}"),
                jobName, jobDescription, jobDisplayName, jobLabel, linkUrl, linkLabel
        );
        Job seed = createSeedJobWithJobDsl(jobDslScript);

        seed.startBuild().shouldSucceed();

        // Verify configuration
        Job job = jenkins.jobs.get(Job.class, jobName);
        job.open();
        assertThat(job.getDescription(), containsString(jobDescription));
        assertThat(job.getDisplayName(), containsString(jobDisplayName));
        assertThat(driver, hasElement(by.href(linkUrl)));

        job.configure();
        WebElement labelElement = driver.findElement(By.xpath("//input[@name='_.label']"));
        assertEquals(jobLabel, labelElement.getAttribute("value"));

        Build build = job.scheduleBuild().waitUntilStarted();
        { // Verify concurrent build
            Build secondBuild = job.scheduleBuild().waitUntilStarted();
            assertTrue(build.isInProgress());
            assertTrue(secondBuild.isInProgress());
            build.stop();
            secondBuild.stop();
            build.waitUntilFinished();
            secondBuild.waitUntilFinished();
        }

        build.open();
        assertThat(build.getDisplayName(), containsString("custom-name"));

        build.open();
        driver.findElement(By.partialLinkText("Environment Variables")).click();
        assertThat(driver, hasElement(by.xpath(String.format("//tr/td[contains(text(), '%s')]", "varname"))));

        assertThat(build.getConsole(), containsString("/tmp/custom-workspace"));
    }

    /**
     * Tests whether a new job with template created by JobDsl exists and includes
     * same description like the template job when the seed job is build.
     */
    @Test
    public void should_create_new_job_by_using_template() {

        // Arrange
        String testedJobName = "MyJob";
        String jobDescription = "My sample despription";
        Job templateJob = jenkins.jobs.create(FreeStyleJob.class, "Template");
        templateJob.configure();
        Control descriptionControl = templateJob.control(by.name("description"));
        descriptionControl.set(jobDescription);
        templateJob.save();
        String jobDslScript = String.format("job('%s') { using('%s') }", testedJobName, templateJob.name);
        Job seed = createSeedJobWithJobDsl(jobDslScript);

        // Act
        seed.scheduleBuild().shouldSucceed();

        // Assert
        Job job = jenkins.jobs.get(Job.class, testedJobName);
        job.open();
        assertThat(job.getDescription(), containsString(jobDescription));
    }

    /**
     * Tests whether an existing job is renamed by a new job with previous names attribute regex
     * created by JobDsl when the seed job is build.
     */
    @Test
    public void should_replace_old_job_with_regex() {

        // Arrange
        String oldJobName = "MyJob01";
        String newJobName = "MyJob02";
        String renameRegex = "/MyJob\\d+/";
        jenkins.jobs.create(FreeStyleJob.class, oldJobName);
        String jobDslScript = String.format("job('%s') { previousNames( %s ) }", newJobName, renameRegex);
        Job seed = createSeedJobWithJobDsl(jobDslScript);

        // Act
        seed.scheduleBuild().shouldSucceed();

        // Assert
        Job job = jenkins.jobs.get(Job.class, newJobName);
        Job oldJob = jenkins.jobs.get(Job.class, oldJobName);
        assertThat(job, pageObjectExists());
        assertThat(oldJob, pageObjectDoesNotExist());
        assertEquals(newJobName, job.name);
    }

    private Job createSeedJobWithJobDsl(String jobDsl) {
        Job seed = jenkins.jobs.create(FreeStyleJob.class, "Seed");
        JobDslBuildStep jobDslBuildStep = seed.addBuildStep(JobDslBuildStep.class);
        jobDslBuildStep.setScript(jobDsl);
        seed.save();

        return seed;
    }

    /**
     * Test if all native columns of a ListView are created and shown correctly.
     */
    @Test
    public void are_columns_created() {
        Job job1 = createJobAndBuild();
        String jobDslScript = "listView('"+ LIST_VIEW_NAME +"') {\n" +
                "  columns {\n" +
                "    status()\n" +
                "    weather()\n" +
                "    name()\n" +
                "    lastSuccess()\n" +
                "    lastFailure()\n" +
                "    lastDuration()\n" +
                "    buildButton()\n" +
                "  }\n" +
                "  jobs{\n" +
                "    name('"+job1.name+"')\n" +
                "  }\n" +
                "}";
        View view = openNewlyCreatedListView(jobDslScript, LIST_VIEW_NAME);
        assertThat(view, containsColumnHeaderTooltip("Status of the last build"));
        assertThat(view, containsColumnHeaderTooltip("Weather report showing aggregated status of recent builds"));
        assertThat(view, containsColumnHeader("Name"));
        assertThat(view, containsColumnHeader("Last Success"));
        assertThat(view, containsColumnHeader("Last Failure"));
        assertThat(view, containsColumnHeader("Last Duration"));

        String searchText = "Schedule a Build for " + job1.name;
        // behaviour change in https://github.com/jenkinsci/jenkins/pull/6084
        WebElement webElement = view.getElement(By.cssSelector(String.format("a[tooltip='%s']", searchText)));
        if (webElement != null) {
            assertThat(view, containsLinkWithTooltip(searchText));
        } else {
            assertThat(view, containsSvgWithText(searchText));
        }
    }

    /**
     * Test if the created jobs are correctly added to the ListView and if the status filter works correctly.
     * In this case only enabled jobs shall be shown.
     */
    @Test
    public void status_filter_only_shows_enabled_jobs() {
        List<Job> jobs = createAmountOfJobs(3, false);
        Job jobDisabled = createDisabledJob();
        String jobDslScript = "listView('"+ LIST_VIEW_NAME +"') {\n" +
                "  statusFilter(StatusFilter.ENABLED)\n" +
                "  columns {\n" +
                "    name()\n" +
                "  }\n" +
                "  jobs {\n" +
                "    names('"+jobs.get(0).name+"', '"+jobs.get(1).name+"', '"+jobs.get(2).name+"', '"+jobDisabled.name+"')\n" +
                "  }\n" +
                "}";
        View view = openNewlyCreatedListView(jobDslScript, LIST_VIEW_NAME);
        assertThat(view, not(containsJob(jobDisabled)));
        assertThat(view, containsJob(jobs.get(0)));
        assertThat(view, containsJob(jobs.get(1)));
        assertThat(view, containsJob(jobs.get(2)));
    }

    /**
     * Test if the created jobs are correctly added to the ListView using a regex.
     */
    @Test
    public void only_jobs_matching_regex_are_added() {
        Job job1 = createJobWithName(EXAMPLE_DISABLED_NAME);
        Job job2 = createJobWithName(EXAMPLE_ENABLED_NAME);
        List<Job> jobs = createAmountOfJobs(2, false);
        String jobDslScript = "listView('"+ LIST_VIEW_NAME +"') {\n" +
                "  columns {\n" +
                "    name()\n" +
                "  }\n" +
                "  jobs {\n" +
                "    regex('"+LIST_VIEW_REGEX+"')\n" +
                "  }\n" +
                "}";
        View view = openNewlyCreatedListView(jobDslScript, LIST_VIEW_NAME);
        assertThat(view, containsJob(job1));
        assertThat(view, containsJob(job2));
        assertThat(view, not(containsJob(jobs.get(0))));
        assertThat(view, not(containsJob(jobs.get(1))));
    }

    /**
     * This test creates a listView and 4 jobs. The job filters are checking the name with a regex and
     * jobs matching the regex are excluded in the listView.
     */
    @Test
    @WithPlugins("view-job-filters")
    public void job_filters_regex_name_exclude_matched() {
        Job job1 = createJobWithName(EXAMPLE_DISABLED_NAME);
        Job job2 = createJobWithName(EXAMPLE_ENABLED_NAME);
        List<Job> jobs = createAmountOfJobs(2, false);
        String jobDslScript = "listView('"+ LIST_VIEW_NAME +"') {\n" +
                "  columns {\n" +
                "    name()\n" +
                "  }\n" +
                "  jobs{\n" +
                "    names('"+job1.name+"','"+job2.name+"','"+jobs.get(0).name+"','"+jobs.get(1).name+"')\n" +
                "  }\n" +
                "  jobFilters {\n" +
                "        regex {\n" +
                "            matchType(MatchType.EXCLUDE_MATCHED)\n" +
                "            matchValue(RegexMatchValue.NAME)\n" +
                "            regex('"+LIST_VIEW_REGEX+"')\n" +
                "        }\n" +
                "  }\n" +
                "}";
        View view = openNewlyCreatedListView(jobDslScript, LIST_VIEW_NAME);
        assertThat(view, not(containsJob(job1)));
        assertThat(view, not(containsJob(job2)));
        assertThat(view, containsJob(jobs.get(0)));
        assertThat(view, containsJob(jobs.get(1)));
    }

    /**
     * This test creates 5 jobs and builds them immediately. A list view is created with its job filter set to show the
     * 3 jobs which were most recently built.
     */
    @Test
    @WithPlugins("view-job-filters")
    public void job_filters_most_recent_3_jobs() {
        List<Job> jobs = createAmountOfJobs(5, true);
        String jobDslScript = "listView('"+ LIST_VIEW_NAME +"') {\n" +
                "  columns {\n" +
                "    name()\n" +
                "  }\n" +
                "  jobs{\n" +
                "    names('"+jobs.get(0).name+"','"+jobs.get(1).name+"','"+jobs.get(2).name+"','"+jobs.get(3).name+"','"+jobs.get(4).name+"')\n" +
                "  }\n" +
                "  jobFilters {\n" +
                "    mostRecent {\n" +
                "      maxToInclude(3)\n" +
                "    }\n" +
                "  }\n" +
                "}";
        View view = openNewlyCreatedListView(jobDslScript, LIST_VIEW_NAME);
        assertThat(view, not(containsJob(jobs.get(0))));
        assertThat(view, not(containsJob(jobs.get(1))));
        assertThat(view, containsJob(jobs.get(2)));
        assertThat(view, containsJob(jobs.get(3)));
        assertThat(view, containsJob(jobs.get(4)));
    }

    /**
     * This test creates 3 jobs and builds 3 of them immediately. One of the fails. A list view is created with its job filter set to only
     * include jobs that failed.
     */
    @Test
    @WithPlugins("view-job-filters")
    public void job_filters_include_failed_jobs_built_at_least_once() {
        List<Job> jobs = createAmountOfJobs(2, true);
        Job job3 = createJobThatFails();
        String jobDslScript = "listView('"+ LIST_VIEW_NAME +"') {\n" +
                "  columns {\n" +
                "    name()\n" +
                "  }\n" +
                "  jobFilters {\n" +
                "    buildTrend {\n" +
                "      matchType(MatchType.INCLUDE_MATCHED)\n" +
                "      buildCountType(BuildCountType.AT_LEAST_ONE)\n" +
                "      status(BuildStatusType.FAILED)\n" +
                "    }\n" +
                "  }\n" +
                "}";
        View view = openNewlyCreatedListView(jobDslScript, LIST_VIEW_NAME);
        assertThat(view, not(containsJob(jobs.get(0))));
        assertThat(view, not(containsJob(jobs.get(1))));
        assertThat(view, containsJob(job3));
    }

    /**
     * This method creates a given amount of jobs and returns them in a List.
     * The caller can choose whether to build the jobs or not.
     * @param amount amount to be created
     * @param isBuilt flag if the jobs should be built
     * @return List of Jobs
     */
    private List<Job> createAmountOfJobs(int amount, boolean isBuilt) {
        List<Job> jobs = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            Job job;
            if (isBuilt) {
                job = createJobAndBuild();
            }
            else {
                job = jenkins.jobs.create(FreeStyleJob.class);
            }
            jobs.add(job);
        }
        return jobs;
    }

    /**
     * This method creates a job which uses a job DSL script that fails its build.
     * @return job that failed
     */
    private Job createJobThatFails() {
        String jobDslScriptFailed = "fail";
        FreeStyleJob job = createJobAndSetJobDslScript(jobDslScriptFailed, false);
        job.scheduleBuild().shouldFail();
        return job;
    }

    /**
     * This method creates a new job and builds it.
     * @return the newly created job
     */
    private Job createJobAndBuild() {
        FreeStyleJob job = jenkins.jobs.create(FreeStyleJob.class);
        job.scheduleBuild();
        return job;
    }

    /**
     * This method creates a new job and disables it.
     * @return the disabled job
     */
    private Job createDisabledJob() {
        Job job = jenkins.jobs.create(FreeStyleJob.class);
        job.disable();
        job.save();
        return job;
    }

    /**
     * This method creates a Job with a given name.
     * @param name name of the job
     * @return newly created job
     */
    private Job createJobWithName(String name) {
        return jenkins.jobs.create(FreeStyleJob.class, name);
    }

    /**
     * This method creates a seed job and configures it with a Job DSL script.
     * @param script The Job DSL scrpt
     * @return The newly created and configured seed job
     */
    private FreeStyleJob createJobAndSetJobDslScript(String script, boolean isSeed) {
        FreeStyleJob job;
        if (isSeed) {
            job = createSeedJob();
        } else {
            job = jenkins.jobs.create(FreeStyleJob.class);
        }
        JobDslBuildStep jobDsl = job.addBuildStep(JobDslBuildStep.class);
        jobDsl.setScript(script);
        job.save();
        return job;
    }

    /**
     * Opens a newly created ListView. The View gets created by a seed job via Job DSL script.
     * @param script The Job DSL script
     */
    private View openNewlyCreatedListView(String script, String viewName) {
        FreeStyleJob seed = createJobAndSetJobDslScript(script, true);
        seed.scheduleBuild().shouldSucceed();
        seed.open();
        View view = getView(viewName);
        view.open();
        return view;
    }

    private FreeStyleJob createSeedJob() {
        return jenkins.jobs.create(FreeStyleJob.class, "Seed");
    }

    private FreeStyleJob getJob(String name) {
        return jenkins.jobs.get(FreeStyleJob.class, name);
    }

    private ListView getView(String name) {
        return jenkins.views.get(ListView.class, name);
    }

    /**
     * Set up global security. Two users 'admin', with admin permission,
     * and 'user', with permissions necessary to manipulate jobs, will be generated.
     * Script security for Job DSL scripts will be enabled.
     * @return The global security configuration.
     */
    private GlobalSecurityConfig setUpSecurity() {
        GlobalSecurityConfig sc = new GlobalSecurityConfig(jenkins);
        sc.configure(() -> {
            MockSecurityRealm ms = sc.useRealm(MockSecurityRealm.class);
            ms.configure(ADMIN,USER);

            MatrixAuthorizationStrategy mas = sc.useAuthorizationStrategy(MatrixAuthorizationStrategy.class);

            MatrixRow a = mas.addUser(ADMIN);
            a.admin();

            MatrixRow b = mas.addUser(USER);
            b.developer();

            sc.setJobDslScriptSecurity(true);
        });
        return sc;
    }

    /**
     * Add project default build access control to the global security configuration.
     * Additionally the permission ist added to 'user' that allows users to run
     * jobs as them on agents. Without this permission the job could not be executed.
     * @param sc A global security configuration.
     */
    private void runBuildAsUserWhoTriggered(GlobalSecurityConfig sc) {
        jenkins.login().doLogin(ADMIN);
        sc.configure(() -> {
            final ProjectDefaultBuildAccessControl control = sc.addBuildAccessControl(ProjectDefaultBuildAccessControl.class);
            control.runAsUserWhoTriggered();

            MatrixAuthorizationStrategy mas = sc.useAuthorizationStrategy(MatrixAuthorizationStrategy.class);
            mas.getUser(USER).on("hudson.model.Computer.Build");
        });
        jenkins.logout();
    }

    /**
     * Check if the description on a {@link ContainerPageObject} match with the
     * given description.
     * @param page  A {@link ContainerPageObject} like {@link FreeStyleJob}.
     * @param description The description that should shown.
     */
    private void checkDescription(ContainerPageObject page, String description) {
        page.open();
        WebElement actual = page.find(By.xpath("//div[@id='description']/div"));
        assertThat(actual.getText(), containsString(description));
    }
}
