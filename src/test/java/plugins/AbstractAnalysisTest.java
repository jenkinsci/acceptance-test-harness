package plugins;

import javax.annotation.CheckForNull;
import javax.mail.MessagingException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.xerces.jaxp.DocumentBuilderFactoryImpl;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.DifferenceConstants;
import org.custommonkey.xmlunit.DifferenceListener;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.Resource;
import org.jenkinsci.test.acceptance.junit.Since;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.analysis_core.AnalysisAction;
import org.jenkinsci.test.acceptance.plugins.analysis_core.AnalysisAction.Tab;
import org.jenkinsci.test.acceptance.plugins.analysis_core.AnalysisConfigurator;
import org.jenkinsci.test.acceptance.plugins.analysis_core.AnalysisMavenSettings;
import org.jenkinsci.test.acceptance.plugins.analysis_core.AnalysisSettings;
import org.jenkinsci.test.acceptance.plugins.analysis_core.GraphConfigurationView;
import org.jenkinsci.test.acceptance.plugins.dashboard_view.AbstractDashboardViewPortlet;
import org.jenkinsci.test.acceptance.plugins.dashboard_view.DashboardView;
import org.jenkinsci.test.acceptance.plugins.email_ext.EmailExtPublisher;
import org.jenkinsci.test.acceptance.plugins.maven.MavenBuildStep;
import org.jenkinsci.test.acceptance.plugins.maven.MavenInstallation;
import org.jenkinsci.test.acceptance.plugins.maven.MavenModuleSet;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.Container;
import org.jenkinsci.test.acceptance.po.Folder;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.FreeStyleMultiBranchJob;
import org.jenkinsci.test.acceptance.po.Job;
import org.jenkinsci.test.acceptance.po.ListView;
import org.jenkinsci.test.acceptance.po.ListViewColumn;
import org.jenkinsci.test.acceptance.po.MatrixProject;
import org.jenkinsci.test.acceptance.po.Node;
import org.jenkinsci.test.acceptance.po.PostBuildStep;
import org.jenkinsci.test.acceptance.po.Slave;
import org.jenkinsci.test.acceptance.po.View;
import org.jenkinsci.test.acceptance.po.WorkflowJob;
import org.jenkinsci.test.acceptance.slave.SlaveController;
import org.jenkinsci.test.acceptance.utils.mail.MailService;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.google.inject.Inject;

import static java.util.Collections.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.jenkinsci.test.acceptance.Matchers.*;

/**
 * Base class for tests of the static analysis plug-ins. Contains several generic test cases that run for all
 * participating plug-ins. Additionally, several helper methods are available for the concrete test cases of a
 * plug-in.
 *
 * @param <P> the type of the project action
 * @author Martin Ende
 * @author Martin Kurz
 * @author Fabian Trampusch
 * @author Ullrich Hafner
 */
public abstract class AbstractAnalysisTest<P extends AnalysisAction> extends AbstractJUnitTest {
    private static final List<String> PRIORITIES = Arrays.asList("HIGH", "LOW", "NORMAL");

    /**
     * Runs a job with warning threshold configured once and validates that build is marked as unstable.
     */
    @Test @Issue("JENKINS-19614")
    public void should_set_build_to_unstable_if_total_warnings_threshold_set() {
        FreeStyleJob job = createFreeStyleJob(jenkins);
        AnalysisAction projectAction = createProjectAction(job);
        editJob(job, projectAction, new AnalysisConfigurator<AnalysisSettings>() {
            @Override
            public void configure(final AnalysisSettings settings) {
                settings.setBuildUnstableTotalAll("0");
                settings.setNewWarningsThresholdFailed("0");
                settings.setUseDeltaValues(true);
            }
        });

        buildJobAndWait(job).shouldBeUnstable();
    }

    /**
     * Sets up a job within a folder. Verifies that the project action from the job redirects to the result
     * of the last build. Checks the correct number of warnings in the project overview and
     * result details view. Finally checks if the warnings-per-project portlet and warnings column show the
     * correct number of warnings and provide a direct link to the actual warning results.
     */
    @Test @Issue({"JENKINS-39947", "JENKINS-39950"}) @WithPlugins({"dashboard-view", "cloudbees-folder"})
    public void should_show_warnings_in_folder() {
        Folder folder = jenkins.jobs.create(Folder.class, jenkins.createRandomName());
        folder.save();

        folder.open();

        FreeStyleJob job = createFreeStyleJob(folder);
        verifyJobResults(job);

        P projectAction = createProjectAction(job);

        addDashboardViewAndBottomPortlet(projectAction.getTablePortlet(), folder);
        verifyPortlet(projectAction);

        addListViewColumn(projectAction.getViewColumn(), folder);
        verifyColumn(projectAction);
    }

    /**
     * Sets up a dashboard view with a warnings-per-project portlet. Builds a job and checks if the portlet shows the
     * correct number of warnings and provides a direct link to the actual warning results.
     */
    @Test @WithPlugins("dashboard-view")
    public void should_show_warning_totals_in_dashboard_portlet_with_link_to_results() {
        FreeStyleJob job = createFreeStyleJob(jenkins);

        buildJobAndWait(job).shouldSucceed();

        P projectAction = createProjectAction(job);
        addDashboardViewAndBottomPortlet(projectAction.getTablePortlet(), jenkins);

        verifyPortlet(projectAction);
    }

    public void verifyPortlet(final P projectAction) {
        List<WebElement> links = all(by.xpath("//td[@class='pane']//a"));

        assertThat(links.size() >= 2, is(true));

        WebElement warningsLink = links.get(links.size() - 1);
        assertThat(warningsLink.getText().trim(), is(String.valueOf(getNumberOfWarnings())));

        warningsLink.click();
        assertThat(driver, hasContent(projectAction.getName()));
    }

    /**
     * Sets up a list view with a warnings column. Builds a job and checks if the column shows the correct number of
     * warnings and provides a direct link to the actual warning results.
     */
    @Test @Issue("JENKINS-24436")
    public void should_show_warning_totals_in_view_column_with_link_to_results() {
        FreeStyleJob job = createFreeStyleJob(jenkins);

        buildJobAndWait(job).shouldSucceed();

        P projectAction = createProjectAction(job);
        addListViewColumn(projectAction.getViewColumn(), jenkins);

        verifyColumn(projectAction);
    }

    public void verifyColumn(final P projectAction) {
        List<WebElement> links = all(by.xpath("//table[@id='projectstatus']//td//a"));

        assertThat(links.isEmpty(), is(false));

        WebElement warningsLink = links.get(links.size() - 1);
        assertThat(warningsLink.getText().trim(), is(String.valueOf(getNumberOfWarnings())));

        warningsLink.click();
        assertThat(driver, hasContent(projectAction.getName()));
    }

    /**
     * Builds a freestyle job with an enabled publisher of the plug-in under test. Sets the thresholds for the trend
     * report so that a health of 0-19% is evaluated for the plug-in under test (shown as tool tip in the Jenkins
     * main view).
     * TODO: Add different health percentages
     */
    @Test @Issue("JENKINS-28360")
    public void should_show_build_health() {
        FreeStyleJob job = createFreeStyleJob(jenkins);

        AnalysisAction projectAction = createProjectAction(job);
        editJob(job, projectAction, new AnalysisConfigurator<AnalysisSettings>() {
            @Override
            public void configure(final AnalysisSettings settings) {
                settings.setBuildHealthyThreshold(0);
                settings.setBuildUnhealthyThreshold(getNumberOfWarnings());
            }
        });

        buildSuccessfulJob(job);

        jenkins.open();

        List<WebElement> healthElements = all(by.xpath("//div[@class='healthReportDetails']//tr"));
        assertThat(healthElements.size(), is(3));

        String expectedText = String.format("%s: %d %s%s found.", projectAction.getPluginName(), getNumberOfWarnings(),
                projectAction.getAnnotationName(), plural(getNumberOfWarnings()));
        assertThatHealthReportIs(healthElements.get(1), expectedText, "00to19");
        assertThatHealthReportIs(healthElements.get(2), "Build stability: No recent builds failed.", "80plus");
    }

    // First td contains icon, second td contains text
    private void assertThatHealthReportIs(final WebElement healthReportTable, final String expectedText, final String expectedIconName) {
        List<WebElement> descriptions = healthReportTable.findElements(By.xpath("td"));

        assertThat(getDescriptionValue(descriptions, 0), containsString(expectedIconName + " "));
        assertThat(getDescriptionValue(descriptions, 1), is(expectedText));
    }

    private String getDescriptionValue(final List<WebElement> descriptions, final int index) {
        return descriptions.get(index).getAttribute("innerHTML");
    }

    private void editJob(final Job job, final AnalysisAction action, final AnalysisConfigurator<AnalysisSettings> configurator) {
        job.configure();
        configurator.configure(job.getPublisher(action.getFreeStyleSettings()));
        job.save();
    }

    /**
     * Builds a freestyle job with an enabled publisher of the plug-in under test. Verifies that the project action from
     * the job redirects to the result of the last build. Then the correct number of warnings in the project overview and
     * result details view are verified.
     */
    @Test
    public void should_navigate_to_result_action_from_freestyle_job() {
        verifyJobResults(createFreeStyleJob(jenkins));
    }

    /**
     * Builds a pipeline with an enabled publisher of the plug-in under test. Verifies that the project action from
     * the job redirects to the result of the last build. Then the correct number of warnings in the project overview and
     * result details view are verified.
     */
    @Test @WithPlugins("workflow-aggregator") @Issue("31202")
    public void should_navigate_to_result_action_from_pipeline() {
        verifyJobResults(createPipeline());
    }

    private void verifyJobResults(final Job job) {
        Build build = buildSuccessfulJob(job);

        P resultAction = createResultAction(build);
        P projectAction = createProjectAction(job);

        projectAction.open();
        assertThat(projectAction.getCurrentUrl(), containsRegexp(resultAction.getUrl()));

        assertThat(resultAction.getNumberOfWarnings(), is(getNumberOfWarnings()));
        assertThat(resultAction.getNumberOfNewWarnings(), is(getNumberOfWarnings()));
        assertThat(resultAction.getNumberOfFixedWarnings(), is(0));

        assertThat(resultAction.getNumberOfWarningsWithHighPriority(), is(getNumberOfHighPriorityWarnings()));
        assertThat(resultAction.getNumberOfWarningsWithNormalPriority(), is(getNumberOfNormalPriorityWarnings()));
        assertThat(resultAction.getNumberOfWarningsWithLowPriority(), is(getNumberOfLowPriorityWarnings()));

        build.open();
        assertThatWarningsCountInSummaryIs(resultAction, getNumberOfWarnings());
        assertThatNewWarningsCountInSummaryIs(resultAction, getNumberOfWarnings());

        assertThat(job, hasAction(projectAction.getName()));
        assertThat(job.getLastBuild(), hasAction(resultAction.getName()));
        assertThat(build, hasAction(resultAction.getName()));

        assertThatDetailsAreFilled(resultAction);
    }

    /**
     * Verifies that the detail views of the results are correctly set. E.g. Sub-classes may check the contents
     * of the individual tabs. This default implementation is empty and should be overwritten.
     *
     * @param resultAction the action containing the results
     */
    protected void assertThatDetailsAreFilled(final P resultAction) {
        // sub-classes may check the contents of the individual tabs
    }

    /**
     * Builds a pipeline with an enabled publisher of the plug-in under test two times in a row. Verifies that
     * afterwards a trend graph exists that contains 6 relative links to the plug-in results (one for each priority and
     * build).
     */
    @Test @WithPlugins("workflow-aggregator") @Issue("31202")
    public void should_have_trend_graph_with_relative_links_in_pipeline() {
        Job job = runTwoTimesInARow(createPipeline());

        verifyTrendGraph(job, getNumberOfWarnings());
    }

    /**
     * Builds a freestyle job with an enabled publisher of the plug-in under test two times in a row. Verifies that
     * afterwards a trend graph exists that contains 6 relative links to the plug-in results (one for each priority and
     * build).
     */
    @Test @Issue({"JENKINS-21723", "JENKINS-29900"})
    public void should_have_trend_graph_with_relative_links_in_freestyle_job() {
        Job job = buildFreestyleJobTwoTimesInARow();

        verifyTrendGraph(job, getNumberOfWarnings());
    }

    protected void verifyTrendGraph(final Job job, final int numberOfWarnings) {
        AnalysisAction action = createProjectAction(job);
        verifyTrendGraphOverview(job, action, numberOfWarnings);
        verifyTrendGraphDetails(job, action, numberOfWarnings);
    }

    private void verifyTrendGraphOverview(final Job job, final AnalysisAction action, final int numberOfWarnings) {
        assertThatProjectPageTrendIsCorrect(job, action, "", numberOfWarnings);
    }

    private void verifyTrendGraphDetails(final Job job, final AnalysisAction action, final int numberOfWarnings) {
        List<WebElement> graphLinks = job.all(By.linkText("Enlarge"));
        graphLinks.get(graphLinks.size() - 1).click();
        assertThatProjectPageTrendIsCorrect(job, action, "../../", numberOfWarnings);
    }

    protected void assertThatProjectPageTrendIsCorrect(final Job job, final AnalysisAction action, final String prefix,
            final int numberOfWarnings) {
        elasticSleep(500);

        Map<String, Integer> trend = job.getTrendGraphContent(action.getUrl());
        assertThat(trend.size(), is(6));

        List<String> actualUrls = new ArrayList<>();
        actualUrls.addAll(trend.keySet());
        sort(actualUrls);

        int index = 0;
        for (int build = 1; build <= 2; build++) {
            int sum = 0;
            for (String priority : PRIORITIES) {
                String expectedUrl = String.format("^%s%d/%sResult/%s$", prefix, build, action.getUrl(), priority);
                String actualUrl = actualUrls.get(index++);
                assertThat(actualUrl, containsRegexp(expectedUrl));
                sum += trend.get(actualUrl);
            }
            assertThat(sum, is(numberOfWarnings));
        }
    }

    /**
     * Runs the test case {@link #should_have_trend_graph_with_relative_links_in_freestyle_job()} with a job that
     * contains a space in the name. Then the trend is deactivated in the trend configuration view: now the trend should
     * be replaced with a link to re-enable the trend. Finally, this link is clicked in order open the trend
     * configuration again.
     */
    @Test @Issue({"JENKINS-25917", "JENKINS-32377"}) @Since("2.0")
    public void should_store_trend_selection_in_cookie() {
        Job job = buildFreestyleJobTwoTimesInARow();

        assertThat(job.name, containsString("_"));
        job = job.renameTo(job.name.replace("_", " "));

        AnalysisAction action = createProjectAction(job);

        verifyTrendGraphOverview(job, action, getNumberOfWarnings());
        deactivateTrendGraph(job, action);
    }

    private Job buildFreestyleJobTwoTimesInARow() {
        return runTwoTimesInARow(createFreeStyleJob(jenkins));
    }

    private Job runTwoTimesInARow(final Job job) {
        buildJobAndWait(job);
        buildSuccessfulJob(job);
        job.open();
        return job;
    }

    private void deactivateTrendGraph(final Job job, final AnalysisAction action) {
        GraphConfigurationView view = action.configureTrendGraphForUser();

        view.open();
        view.deactivateTrend();
        view.save();

        List<WebElement> enableLinks = job.all(By.partialLinkText("Enable"));
        assertThat(enableLinks.size(), is(1));

        enableLinks.get(0).click();
        elasticSleep(500);

        assertThat(getCurrentUrl(), containsRegexp(action.getUrl() + "/configure/"));
    }

    /**
     * Creates a specific project action of the plug-in under test.
     *
     * @param job the job containing this action
     * @return the created action
     */
    protected abstract P createProjectAction(final Job job);

    /**
     * Creates a specific result action of the plug-in under test.
     *
     * @param build the build containing the results for this action
     * @return the created action
     */
    protected abstract P createResultAction(final Build build);

    /**
     * Creates a freestyle job that has an enabled publisher of the plug-in under test. The job is expected to run with
     * build status SUCCESS.
     *
     * @param owner the owner of the job (Jenkins or a folder)
     * @return the created freestyle job
     */
    protected abstract FreeStyleJob createFreeStyleJob(final Container owner);

    /**
     * Creates a pipeline that has an enabled publisher of the plug-in under test. The job is expected to run with
     * build status SUCCESS.
     *
     * @return the created pipeline
     */
    protected abstract WorkflowJob createPipeline();

    /**
     * Creates a pipeline that enables the specified {@code stepName}. The first step of the pipeline copies the
     * specified resource {@code fileName} as first instruction to the pipeline.
     *
     * @param fileName the name of the resource that will be copied to the pipeline (this file will be scanned for
     *                 warnings)
     * @param stepName the name of the publisher to run (as a pipeline step)
     * @return the created pipeline
     */
    protected WorkflowJob createPipelineWith(final String fileName, final String stepName) {
        WorkflowJob job = jenkins.jobs.create(WorkflowJob.class);
        String script = "node {\n"
                + job.copyResourceStep(fileName)
                + "  step([$class: '" + stepName
                + "', pattern: '**/" + FilenameUtils.getName(fileName)
                + "'])\n" +
                "}";
        job.script.set(script);
        job.sandbox.check();
        job.save();
        return job;
    }

    /**
     * Returns the number of warnings that the created jobs should produce.
     *
     * @return total number of warnings
     * @see #createFreeStyleJob(Container)
     * @see #createPipeline()
     */
    protected abstract int getNumberOfWarnings();

    /**
     * Returns the number of warnings with priority HIGH that the created jobs should produce.
     *
     * @return total number of warnings (priority HIGH)
     * @see #createFreeStyleJob(Container)
     * @see #createPipeline()
     */
    protected abstract int getNumberOfHighPriorityWarnings();

    /**
     * Returns the number of warnings with priority NORMAL that the created jobs should produce.
     *
     * @return total number of warnings (priority NORMAL)
     * @see #createFreeStyleJob(Container)
     * @see #createPipeline()
     */
    protected abstract int getNumberOfNormalPriorityWarnings();

    /**
     * Returns the number of warnings with priority LOW that the created jobs should produce.
     *
     * @return total number of warnings (priority LOW)
     * @see #createFreeStyleJob(Container)
     * @see #createPipeline()
     */
    protected abstract int getNumberOfLowPriorityWarnings();

    /** Mock that verifies that mails have been sent by Jenkins email-ext plugin. */
    @Inject
    private MailService mail;

    /** Provides slaves for tests that need build slaves. */
    @Inject
    private SlaveController slaveController;

    /**
     * Configures the mailer with default values required for the mock.
     */
    protected void setUpMailer() {
        mail.setup(jenkins);
    }

    /**
     * Configures the mail notification of the email-ext plug-in.
     *
     * @param job     the job to configure
     * @param subject subject of the mail
     * @param body    body of the mail
     */
    protected void configureEmailNotification(final FreeStyleJob job, final String subject, final String body) {
        job.configure();
        EmailExtPublisher pub = job.addPublisher(EmailExtPublisher.class);
        pub.subject.set(subject);
        pub.setRecipient("dev@example.com");
        pub.body.set(body);
        job.save();
    }

    /**
     * Verifies that Jenkins sent a mail with the specified content.
     *
     * @param subject the expected subject of the mail
     * @param body    the expected body of the mail
     */
    protected void verifyReceivedMail(final String subject, final String body) {
        try {
            mail.assertMail(Pattern.compile(subject), "dev@example.com", Pattern.compile(body));
        }
        catch (MessagingException e) {
            throw new IllegalStateException("Mailer exception", e);
        }
        catch (IOException e) {
            throw new IllegalStateException("Mailer exception", e);
        }
    }

    /**
     * Set up a Job of a certain type with a given resource and a publisher which can be configured by providing a
     * configurator
     *
     * @param resourceToCopy              Resource to copy to build (Directory or File path)
     * @param jobClass                    the type the job shall be created of, e.g. FreeStyleJob
     * @param publisherBuildSettingsClass the type of the publisher to be added
     * @param owner
     * @param configurator                the configuration of the publisher
     * @return the new job
     */
    public <J extends Job, T extends AnalysisSettings & PostBuildStep> J setupJob(String resourceToCopy,
            Class<J> jobClass, Class<T> publisherBuildSettingsClass,
            final Container owner, AnalysisConfigurator<T> configurator) {
        return setupJob(resourceToCopy, jobClass, publisherBuildSettingsClass, null, owner, configurator);
    }

    /**
     * Set up a Job of a certain type with a given resource and a publisher which can be configured by providing a
     * configurator
     *
     * @param resourceToCopy              Resource to copy to build (Directory or File path)
     * @param jobClass                    the type the job shall be created of, e.g. FreeStyleJob
     * @param publisherBuildSettingsClass the type of the publisher to be added
     * @param goal                        a maven goal to be added to the job or null otherwise
     * @param owner                       the owner of the job (Jenkins or a folder)
     * @param configurator                the configuration of the publisher
     * @return the new job
     */
    public <J extends Job, T extends AnalysisSettings & PostBuildStep> J setupJob(String resourceToCopy,
            Class<J> jobClass, Class<T> publisherBuildSettingsClass,
            String goal, final Container owner, AnalysisConfigurator<T> configurator) {
        if (jobClass.isAssignableFrom(MavenModuleSet.class)) {
            MavenInstallation.ensureThatMavenIsInstalled(jenkins);
        }

        J job = owner.getJobs().create(jobClass);
        job.configure();

        if (resourceToCopy != null) {
            // first copy resource and then add a goal
            addResourceToJob(job, resourceToCopy);
        }

        // check if a goal is defined and configure the job depending on the job class
        if (goal != null) {
            if (jobClass.isAssignableFrom(MavenModuleSet.class)) {
                ((MavenModuleSet) job).goals.set(goal);
            }
            else if (isFreeStyleOrMatrixJob(jobClass)) {
                job.addBuildStep(MavenBuildStep.class).targets.set(goal);
            }
        }

        T buildSettings = null;

        if (jobClass.isAssignableFrom(MavenModuleSet.class)) {
            buildSettings = ((MavenModuleSet) job).addBuildSettings(publisherBuildSettingsClass);
        }
        else if (isFreeStyleOrMatrixJob(jobClass)) {
            buildSettings = job.addPublisher(publisherBuildSettingsClass);
        }

        if ((buildSettings != null) && (configurator != null)) {
            configurator.configure(buildSettings);
        }

        job.save();
        return job;
    }

    private <J extends Job> boolean isFreeStyleOrMatrixJob(final Class<J> jobClass) {
        return jobClass.isAssignableFrom(FreeStyleJob.class)
                || jobClass.isAssignableFrom(MatrixProject.class)
                || jobClass.isAssignableFrom(FreeStyleMultiBranchJob.class);
    }

    /**
     * Provides the ability to edit an existing job by changing or adding the resource to copy and/or by changing the
     * configuration of a publisher
     *
     * @param newResourceToCopy    the new resource to be copied to build (Directory or File path) or null if not to be
     *                             changed
     * @param isAdditionalResource decides whether the old resource is kept (FALSE) or deleted (TRUE)
     * @param job                  the job to be changed
     * @return the edited job
     */
    public <J extends Job, T extends AnalysisSettings & PostBuildStep> J editJob(String newResourceToCopy,
            boolean isAdditionalResource,
            J job) {
        return edit(newResourceToCopy, isAdditionalResource, job, null, null);
    }

    /**
     * Provides the ability to edit an existing job by changing or adding the resource to copy and/or by changing the
     * configuration of a publisher
     *
     * @param newResourceToCopy           the new resource to be copied to build (Directory or File path) or null if not
     *                                    to be changed
     * @param isAdditionalResource        decides whether the old resource is kept (FALSE) or deleted (TRUE)
     * @param job                         the job to be changed
     * @param publisherBuildSettingsClass the type of the publisher to be modified
     * @param configurator                the new configuration of the publisher
     * @return the edited job
     */
    public <J extends Job, T extends AnalysisSettings & PostBuildStep> J editJob(String newResourceToCopy,
            boolean isAdditionalResource,
            J job,
            Class<T> publisherBuildSettingsClass,
            AnalysisConfigurator<T> configurator) {
        return edit(newResourceToCopy, isAdditionalResource, job, publisherBuildSettingsClass, configurator);
    }

    /**
     * Provides the ability to edit an existing job by changing or adding the resource to copy and/or by changing the
     * configuration of a publisher
     *
     * @param job                         the job to be changed
     * @param publisherBuildSettingsClass the type of the publisher to be modified
     * @param configurator                the new configuration of the publisher
     * @return the edited job
     */
    public <J extends Job, T extends AnalysisSettings & PostBuildStep> J editJob(
            final J job,
            Class<T> publisherBuildSettingsClass,
            AnalysisConfigurator<T> configurator) {
        return edit(null, false, job, publisherBuildSettingsClass, configurator);
    }

    /**
     * Provides the ability to edit an existing job by changing or adding the resource to copy and/or by changing the
     * configuration of a publisher
     *
     * @param isAdditionalResource        decides whether the old resource is kept (FALSE) or deleted (TRUE)
     * @param job                         the job to be changed
     * @param publisherBuildSettingsClass the type of the publisher to be modified
     * @param configurator                the new configuration of the publisher
     * @return the edited job
     */
    public <J extends Job, T extends AnalysisSettings & PostBuildStep> J editJob(boolean isAdditionalResource,
            J job,
            Class<T> publisherBuildSettingsClass,
            AnalysisConfigurator<T> configurator) {
        return edit(null, isAdditionalResource, job, publisherBuildSettingsClass, configurator);
    }

    /**
     * Provides the ability to edit an existing job by changing or adding the resource to copy and/or by changing the
     * configuration of a publisher
     *
     * @param newResourceToCopy           the new resource to be copied to build (Directory or File path) or null if not
     *                                    to be changed
     * @param isAdditionalResource        decides whether the old resource is kept (FALSE) or deleted (TRUE)
     * @param job                         the job to be changed
     * @param publisherBuildSettingsClass the type of the publisher to be modified
     * @param configurator                the new configuration of the publisher
     * @return the edited job
     */
    private <J extends Job, T extends AnalysisSettings & PostBuildStep> J edit(
            @CheckForNull final String newResourceToCopy, final boolean isAdditionalResource,
            final J job, final Class<T> publisherBuildSettingsClass, @CheckForNull AnalysisConfigurator<T> configurator) {
        job.configure();

        if (newResourceToCopy != null) {
            //check whether to exchange the copy resource shell step
            if (!isAdditionalResource) {
                job.removeFirstBuildStep();
                elasticSleep(1000); // chrome needs some time
            }

            //add the new copy resource shell step
            addResourceToJob(job, newResourceToCopy);
        }

        // change the configuration of the publisher
        if (configurator != null) {
            configurator.configure(job.getPublisher(publisherBuildSettingsClass));
        }

        job.save();
        return job;
    }


    /**
     * Creates a slave and configures thes specified job to run on that slave.
     *
     * @param job job to run on slave
     * @return created slave
     */
    public Slave createSlaveForJob(final Job job) {
        try {
            Slave slave = slaveController.install(jenkins).get();
            job.configure();
            job.setLabelExpression(slave.getName());
            job.save();
            return slave;
        }
        catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Can't create Slave", e);
        }
    }

    /**
     * Setup a maven build.
     *
     * @param resourceProjectDir     A Folder in resources which shall be copied to the working directory. Should
     *                               contain the pom.xml
     * @param goal                   The maven goals to set.
     * @param codeStyleBuildSettings The code analyzer to use or null if you do not want one.
     * @param configurator           A configurator to custommize the code analyzer settings you want to use.
     * @param <T>                    The type of the Analyzer.
     * @return The configured job.
     */
    public <T extends AnalysisMavenSettings> MavenModuleSet setupMavenJob(String resourceProjectDir,
            String goal,
            Class<T> codeStyleBuildSettings,
            AnalysisConfigurator<T> configurator) {
        MavenInstallation.ensureThatMavenIsInstalled(jenkins);

        MavenModuleSet job = jenkins.jobs.create(MavenModuleSet.class);
        job.copyDir(resource(resourceProjectDir));
        job.goals.set(goal);


        T buildSettings = job.addBuildSettings(codeStyleBuildSettings);

        if (configurator != null) {
            configurator.configure(buildSettings);
        }

        job.save();

        return job;
    }

    /**
     * Build Job and wait until finished.
     *
     * @param job Job to build
     * @return The made build
     */
    public Build buildJobAndWait(Job job) {
        return job.startBuild().waitUntilFinished();
    }

    /**
     * Builds the job and waits until the job has been finished. The build result must be SUCCESS.
     *
     * @param job the job to build
     * @return the successful build
     */
    public Build buildSuccessfulJob(Job job) {
        return buildJobAndWait(job).shouldSucceed();
    }

    /**
     * Builds the job and waits until the job has been finished. The build result must be FAILURE.
     *
     * @param job the job to build
     * @return the failed build
     */
    protected Build buildFailingJob(final Job job) {
        return buildJobAndWait(job).shouldFail();
    }

    /**
     * Builds the job and waits until the job has been finished. The build result must be UNSTABLE.
     *
     * @param job the job to build
     * @return the unstable build
     */
    protected Build buildUnstableJob(final Job job) {
        return buildJobAndWait(job).shouldBeUnstable();
    }

    /**
     * Builds the job on the specified slave and waits until the job has been finished. The build result must be
     * SUCCESS.
     *
     * @param job   the job to build
     * @param slave the slave to run the job on
     * @return the successful build
     */
    public Build buildSuccessfulJobOnSlave(final Job job, final Node slave) {
        return job.startBuild(singletonMap("slavename", slave.getName())).shouldSucceed();
    }

    /**
     * When Given a finished build, an API-Url and a reference XML-File, this method compares if the api call to the
     * build matches the expected XML-File. Whitespace differences are ignored.
     *
     * @param build                 The build, whose api shall be called.
     * @param apiUrl                The API-Url, declares which build API shall be called.
     * @param expectedXmlPath       The Resource-Path to a file, which contains the expected XML
     * @param ignoreAttributesDiffs whether to ignore attribute differences
     */
    protected void assertXmlApiMatchesExpected(final Build build, final String apiUrl,
            final String expectedXmlPath, final boolean ignoreAttributesDiffs) {
        try {
            XMLUnit.setIgnoreWhitespace(true);
            String xmlUrl = build.url(apiUrl).toString();
            DocumentBuilder documentBuilder = null;
            documentBuilder = DocumentBuilderFactoryImpl.newInstance().newDocumentBuilder();

            Document actual = documentBuilder.parse(xmlUrl);
            Document expected = documentBuilder.parse(resource(expectedXmlPath).asFile());
            Diff diff = new Diff(expected, actual);
            if (ignoreAttributesDiffs) {
                diff.overrideDifferenceListener(new IgnoreAttributesDifferenceListener());
            }
            XMLAssert.assertXMLEqual(diff, true);
        }
        catch (ParserConfigurationException | SAXException | IOException exception) {
            throw new RuntimeException("Can't verify API XML", exception);
        }
    }

    /**
     * Creates a new view and adds the given column to the view.
     *
     * @param columnClass the type of the column to add
     * @param owner       the owner of this view
     * @param <T>         the concrete type of the ListViewColumn
     * @return the created view
     */
    protected <T extends ListViewColumn> ListView addListViewColumn(final Class<T> columnClass, final Container owner) {
        ListView view = createNewViewForAllJobs(ListView.class, owner);
        view.addColumn(columnClass);
        view.save();
        return view;
    }

    /**
     * Creates a new view with a random name that matches all jobs.
     *
     * @param viewClass the type of the view to add
     * @param owner     the owner of this view
     * @param <T>       the type constraint of the view
     * @return the created view
     */
    private <T extends View> T createNewViewForAllJobs(final Class<T> viewClass, final Container owner) {
        T view = owner.getViews().create(viewClass, jenkins.createRandomName());
        view.configure();
        view.matchAllJobs();
        return view;
    }

    /**
     * Adds a shell step to a given job to copy resources to the job's workspace.
     *
     * @param job            the job the resource shall be added to
     * @param resourceToCopy Resource to copy to build (Directory or File path)
     */
    protected <J extends Job> J addResourceToJob(J job, String resourceToCopy) {

        Resource res = resource(resourceToCopy);
        //decide whether to utilize copyResource or copyDir
        if (res.asFile().isDirectory()) {
            job.copyDir(res);
        }
        else {
            job.copyResource(res);
        }

        return job;
    }

    /**
     * Creates a new Dashboard-View and adds the given portlet as "bottom portlet".
     *
     * @param <T>     the type constraint of the portlet
     * @param portlet the portlet to add
     * @param owner   the owner of the view
     * @return The view.
     */
    protected <T extends AbstractDashboardViewPortlet> DashboardView addDashboardViewAndBottomPortlet(
            final Class<T> portlet, final Container owner) {
        DashboardView view = createNewViewForAllJobs(DashboardView.class, owner);
        view.addBottomPortlet(portlet);
        view.save();
        return view;
    }

    /**
     * Verifies that the source code of an affected file is correctly visualized. The specified tab is used to find the
     * file with the warning. On this tab the corresponding link is clicked and the source file should be shown in a new
     * page.
     *
     * @param action          the action holding the results
     * @param file            the affected file that contains the warning
     * @param line            the affected line number
     * @param expectedContent the expected content of the source line (includes the line number)
     * @param expectedToolTip a substring that should be part of the warning tool tip
     */
    protected void verifySourceLine(final AnalysisAction action, final String file, final int line,
            final String expectedContent, final String expectedToolTip) {
        Tab tabId = Tab.DETAILS;
        assertThat(action.getLinkedSourceFileLineNumber(tabId, file, line), is(line));
        assertThat(action.getLinkedSourceFileText(tabId, file, line), startsWith(expectedContent));
        assertThat(action.getLinkedSourceFileToolTip(tabId, file, line), containsString(expectedToolTip));
    }

    /**
     * Verifies that in the summary page of the specified action there is a link that references all warnings. The link
     * label contains the specified number of warnings.
     *
     * @param action           the action to check
     * @param numberOfWarnings the number of warnings
     */
    protected void assertThatWarningsCountInSummaryIs(final AnalysisAction action, final int numberOfWarnings) {
        assertThatLinkReferencesNumberOfWarnings(action, numberOfWarnings, "", "");
    }

    /**
     * Verifies that in the summary page of the specified action there is a link that references the new warnings. The
     * link label contains the specified number of new warnings.
     *
     * @param action              the action to check
     * @param numberOfNewWarnings the number of new warnings
     */
    protected void assertThatNewWarningsCountInSummaryIs(final AnalysisAction action, final int numberOfNewWarnings) {
        assertThatLinkReferencesNumberOfWarnings(action, numberOfNewWarnings, "new ", "/new");
    }

    /**
     * Verifies that in the summary page of the specified action there is a link that references the fixed warnings. The
     * link label contains the specified number of fixed warnings.
     *
     * @param action                the action to check
     * @param numberOfFixedWarnings the number of fixed warnings
     */
    protected void assertThatFixedWarningsCountInSummaryIs(final AnalysisAction action, final int numberOfFixedWarnings) {
        assertThatLinkReferencesNumberOfWarnings(action, numberOfFixedWarnings, "fixed ", "/fixed");
    }

    private void assertThatLinkReferencesNumberOfWarnings(final AnalysisAction action, final int numberOfWarnings, final String linkText, final String url) {
        assertThat(action.getResultLinkByXPathText(numberOfWarnings + " " + linkText + action.getAnnotationName() + plural(numberOfWarnings)),
                containsRegexp(action.getUrl()));
    }

    protected String plural(final int numberOfWarnings) {
        return numberOfWarnings == 1 ? StringUtils.EMPTY : "s";
    }

    /**
     * Verifies that the specified build contains no warnings.
     *
     * @param build the build to verify
     */
    protected void assertThatBuildHasNoWarnings(final Build build) {
        assertThat(build.open(), hasContent("0 warnings"));
    }

    /**
     * Difference listener that overrides the default one if attributes related different
     * need to be ignored.
     */
    public static class IgnoreAttributesDifferenceListener implements DifferenceListener {

        private static final int[] IGNORE = new int[]{
                DifferenceConstants.ATTR_NAME_NOT_FOUND_ID,
                DifferenceConstants.ELEMENT_NUM_ATTRIBUTES_ID
        };

        @Override
        public int differenceFound(Difference difference) {
            return Arrays.binarySearch(IGNORE, difference.getId()) >= 0
                    ? RETURN_IGNORE_DIFFERENCE_NODES_SIMILAR
                    : RETURN_ACCEPT_DIFFERENCE;
        }

        @Override
        public void skippedComparison(org.w3c.dom.Node control, org.w3c.dom.Node test) {
        }
    }
}
