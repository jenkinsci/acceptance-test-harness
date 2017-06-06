package plugins;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.authorize_project.ProjectDefaultBuildAccessControl;
import org.jenkinsci.test.acceptance.plugins.config_file_provider.ConfigFileProvider;
import org.jenkinsci.test.acceptance.plugins.job_dsl.*;
import org.jenkinsci.test.acceptance.plugins.matrix_auth.MatrixAuthorizationStrategy;
import org.jenkinsci.test.acceptance.plugins.matrix_auth.MatrixRow;
import org.jenkinsci.test.acceptance.plugins.mock_security_realm.MockSecurityRealm;
import org.jenkinsci.test.acceptance.plugins.script_security.ScriptApproval;
import org.jenkinsci.test.acceptance.po.*;
import org.jenkinsci.test.acceptance.update_center.PluginSpec;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.regex.Pattern;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.jenkinsci.test.acceptance.Matchers.*;
import static org.junit.Assume.assumeTrue;

/**
 * Acceptance tests for the Job DSL plugin.
 *
 * @author Maximilian Oeckler
 */
@WithPlugins("job-dsl")
public class JobDslPluginTest extends AbstractJUnitTest {

    private static String ADMIN = "admin";
    private static String USER = "user";
    /**
     * Tests if the checkbox ignoreMissingFiles is shown when the
     * radiobutton 'Look on Filesystem' is selected,
     * and not shown if the radiobutton 'Use the provided DSL script'
     * is selected.
     */
    @Test
    public void is_ignoreMissingFiles_shown_right() {
        FreeStyleJob seedJob = createSeedJob();
        JobDslBuildStep jobDsl = seedJob.addBuildStep(JobDslBuildStep.class);
        assertThat(jobDsl.isIgnoreMissingFilesShown(), is(true));
        jobDsl.clickUseScriptText();
        assertThat(jobDsl.isIgnoreMissingFilesShown(), is(false));
        jobDsl.clickLookOnFilesystem();
        assertThat(jobDsl.isIgnoreMissingFilesShown(), is(true));
    }

    /**
     * Verifies that all configurations, done on the job configuration page,
     * are saved correctly.
     */
    @Test
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

        seedJob.edit(() -> jobDsl.setUseSandbox(true));
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
        seedJob.edit(() -> jobDsl.setIgnoreMissingFiles(true));
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

        seedJob.edit(() -> {
            jobDsl.setScript("job('Existing_Job') {\n" +
                             "   description('This is a description');\n" +
                             "}");
            jobDsl.setIgnoreExisting(true);
        });
        Build build = seedJob.scheduleBuild().shouldSucceed();

        Pattern expected = Pattern.compile("Existing items:(\\s*)GeneratedJob[{]name='Existing_Job'}");
        assertThat(build.getConsole(), containsRegexp(expected));
        checkDescription(existingJob, "Existing description");

        seedJob.edit(() -> jobDsl.setIgnoreExisting(false));
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

        seedJob.edit(() -> {
            jobDsl.setScript("listView('Existing_View') {\n" +
                    "   description('This is a description');\n" +
                    "}");
            jobDsl.setIgnoreExisting(true);
        });
        Build build = seedJob.scheduleBuild().shouldSucceed();

        Pattern expected = Pattern.compile("Existing views:(\\s*)GeneratedView[{]name='Existing_View'}");
        assertThat(build.getConsole(), containsRegexp(expected));
        checkDescription(existingView, "Existing description");

        seedJob.edit(() -> jobDsl.setIgnoreExisting(false));
        Build build2 = seedJob.scheduleBuild().shouldSucceed();
        assertThat(build2.getConsole(), containsRegexp(expected));
        checkDescription(existingView, "This is a description");
    }

    /**
     * Verifies whether a previously generated job will be ignored if it
     * is not referenced anymore.
     */
    @Test
    public void should_ignore_removed_Jobs() {
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
    public void should_disable_removed_Jobs() {
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
    public void should_delete_removed_Jobs() {
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

        seedJob.edit(() -> {
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

        seedJob.edit(() -> {
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
    @Test
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

        seedJob.edit(() -> {
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
        assumeTrue("This test requires a restartable Jenkins", jenkins.canRestart());
        // check if plugin is installed. if true, disable plugin
        PluginSpec pluginSpec = new PluginSpec("chucknorris");
        PluginManager pm = jenkins.getPluginManager();
        if (pm.isInstalled(pluginSpec)) {
            pm.enablePlugin(pluginSpec, false);
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
        assertThat(build.getConsole(), containsString("Warning: (script, line 3) plugin 'chucknorris' needs to be installed"));

        seedJob.edit(() -> jobDsl.setFailOnMissingPlugin(true));
        build = seedJob.scheduleBuild().shouldFail();
        assertThat(build.getConsole(), containsString("ERROR: (script, line 3) plugin 'chucknorris' needs to be installed"));
    }

    /**
     * Verifies whether the build will be marked as unstable if deprecated
     * features are used.
     * By default, only a warning is printed to the build log.
     * If the function is used, the build will be marked as unstable.
     */
    @Test @WithPlugins("config-file-provider")
    public void should_unstable_on_deprecated_features() {
        FreeStyleJob seedJob = createSeedJob();
        JobDslBuildStep jobDsl = seedJob.addBuildStep(JobDslBuildStep.class);
        jobDsl.setScript("customConfigFile('New_Config_File')");
        seedJob.save();
        Build build = seedJob.scheduleBuild().shouldSucceed();
        assertThat(build.getConsole(), containsString("Warning: (script, line 1) customConfigFile is deprecated"));
        seedJob.edit(() -> jobDsl.setUnstableOnDeprecation(true));
        build = seedJob.scheduleBuild().shouldBeUnstable();
        assertThat(build.getConsole(), containsString("Warning: (script, line 1) customConfigFile is deprecated"));
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

        seedJob.edit(() -> jobDsl.setAdditionalClasspath("src", "Utility.jar"));

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
    @Test @WithPlugins({"matrix-auth","mock-security-realm"})
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

        sc.edit(() -> sc.setJobDslScriptSecurity(false));

        jenkins.logout();
        jenkins.login().doLogin(USER);

        // Build should succeed because script is approved now
        seedJob.scheduleBuild().shouldSucceed();
        getJob("New_Job").open();
    }

    /**
     * Verifies that if script security for Job DSL scripts is enabled and Jenkins
     * security is enabled, it is not possible to import Groovy classes from the
     * workspace.
     */
    @Test @WithPlugins({"matrix-auth","mock-security-realm"})
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
        assertThat(build.getConsole(), containsString("unable to resolve class utilities.MyUtilities"));

        sc.edit(() -> sc.setJobDslScriptSecurity(false));

        seedJob.scheduleBuild().shouldSucceed();
    }

    /**
     * Verifies that if script security for Job DSL scripts is enabled,
     * scripts saved by non administrators that not run in a Groovy sandbox
     * wont be executed.
     * Administrators can approve scripts in the 'Script Approval' of the
     * 'Manage Jenkins' area. Approved scripts can be executed.
     */
    @Test @WithPlugins({"matrix-auth","mock-security-realm"})
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
        getJob("New_Job").open();
    }

    /**
     * Verifies that if script security for Job DSL scripts is enabled,
     * scripts saved by non administrators that not run in a Groovy sandbox
     * wont be executed.
     * If a administrator saves the seed job, any DSL scripts it contains
     * will be automatically approved. Afterwards the script
     * can be executed.
     */
    @Test @WithPlugins({"matrix-auth","mock-security-realm"})
    public void should_approve_administrator_script_automatically() {
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
        seedJob.configure();
        seedJob.save();

        jenkins.logout();
        jenkins.login().doLogin(USER);

        // Build should succeed because job was saved from administrator
        seedJob.scheduleBuild().shouldSucceed();
        getJob("New_Job").open();
    }

    /**
     * Verifies that if script security for Job DSL scripts is enabled,
     * scripts saved by non administrators can run in a Groovy sandbox
     * without approval. All Job DSL methods are whitelisted by default.
     */
    @Test @WithPlugins({"matrix-auth","mock-security-realm","authorize-project"})
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

        seedJob.edit(() -> jobDsl.setUseSandbox(true));

        // Build should succeed because the script runs in Groovy sandbox
        // and only Job DSL methods are used.
        seedJob.scheduleBuild().shouldSucceed();
        getJob("New_Job").open();
    }

    /**
     * Verifies that if script security for Job DSL scripts is enabled,
     * scripts with not whitelisted content saved by non administrators
     * wont be executed even it should run in a Groovy sandbox.
     * Administrators can approve this content in the 'Script Approval' of the
     * 'Manage Jenkins' area. Approved scripts can be executed.
     */
    @Test @WithPlugins({"matrix-auth","mock-security-realm","authorize-project"})
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
        getJob("New_Job").open();
    }


    /**
     * Verifies that Groovy sandbox can only used if 'Access Control for Builds'
     * is configured. The DSL job needs to run as a particular user.
     */
    @Test @WithPlugins({"matrix-auth","mock-security-realm","authorize-project"})
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
        getJob("New_Job").open();
    }

    /**
     * Verifies that the sidebar link 'Job DSL API Reference' on the project page of a job
     * is only shown if the job contains a Job DSL build step.
     * Further it checks that the API page could be shown if the link was clicked.
     */
    @Test
    public void should_show_Job_Dsl_Api_Reference() {
        String hrefLocator = "/plugin/job-dsl/api-viewer/index.html";

        FreeStyleJob seedJob = createSeedJob();
        seedJob.open();
        assertThat(driver, not(hasElement(by.href(hrefLocator))));

        seedJob.edit(() -> seedJob.addBuildStep(JobDslBuildStep.class));

        find(by.href(hrefLocator)).click();
        assertThat(driver, hasElement(by.link("Jenkins Job DSL Plugin")));

        seedJob.edit(seedJob::removeFirstBuildStep);
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

        generatedJob.description("New Description", false);
        assertThat(driver, hasContent(alert));
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
        sc.edit(() -> {
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
        sc.edit(() -> {
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
