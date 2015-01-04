package plugins;

import javax.annotation.CheckForNull;
import javax.mail.MessagingException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

import org.apache.xerces.jaxp.DocumentBuilderFactoryImpl;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.Resource;
import org.jenkinsci.test.acceptance.plugins.analysis_core.AnalysisConfigurator;
import org.jenkinsci.test.acceptance.plugins.analysis_core.AnalysisMavenSettings;
import org.jenkinsci.test.acceptance.plugins.analysis_core.AnalysisSettings;
import org.jenkinsci.test.acceptance.plugins.dashboard_view.AbstractDashboardViewPortlet;
import org.jenkinsci.test.acceptance.plugins.dashboard_view.DashboardView;
import org.jenkinsci.test.acceptance.plugins.email_ext.EmailExtPublisher;
import org.jenkinsci.test.acceptance.plugins.mailer.MailerGlobalConfig;
import org.jenkinsci.test.acceptance.plugins.maven.MavenBuildStep;
import org.jenkinsci.test.acceptance.plugins.maven.MavenInstallation;
import org.jenkinsci.test.acceptance.plugins.maven.MavenModuleSet;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.Job;
import org.jenkinsci.test.acceptance.po.ListView;
import org.jenkinsci.test.acceptance.po.ListViewColumn;
import org.jenkinsci.test.acceptance.po.MatrixProject;
import org.jenkinsci.test.acceptance.po.Node;
import org.jenkinsci.test.acceptance.po.PostBuildStep;
import org.jenkinsci.test.acceptance.po.Slave;
import org.jenkinsci.test.acceptance.po.View;
import org.jenkinsci.test.acceptance.slave.SlaveController;
import org.jenkinsci.test.acceptance.utils.mail.MailService;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.google.inject.Inject;

import static java.util.Collections.*;
import static org.junit.Assert.*;

/**
 * Base class for tests of the static analysis plug-ins.
 */
public abstract class AbstractAnalysisTest extends AbstractJUnitTest {
    /** Configuration of the mailing in Jenkins global configuration screen. */
    @Inject
    private MailerGlobalConfig mailer;

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
        jenkins.configure();
        mailer.setupDefaults();
        jenkins.save();
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
     * @param configurator                the configuration of the publisher
     * @return the new job
     */
    public <J extends Job, T extends AnalysisSettings & PostBuildStep> J setupJob(String resourceToCopy,
                                                                                  Class<J> jobClass,
                                                                                  Class<T> publisherBuildSettingsClass,
                                                                                  AnalysisConfigurator<T> configurator) {
        return setupJob(resourceToCopy, jobClass, publisherBuildSettingsClass, configurator, null);
    }

    /**
     * Set up a Job of a certain type with a given resource and a publisher which can be configured by providing a
     * configurator
     *
     * @param resourceToCopy              Resource to copy to build (Directory or File path)
     * @param jobClass                    the type the job shall be created of, e.g. FreeStyleJob
     * @param publisherBuildSettingsClass the type of the publisher to be added
     * @param configurator                the configuration of the publisher
     * @param goal                        a maven goal to be added to the job or null otherwise
     * @return the new job
     */
    public <J extends Job, T extends AnalysisSettings & PostBuildStep> J setupJob(String resourceToCopy,
                                                                                  Class<J> jobClass,
                                                                                  Class<T> publisherBuildSettingsClass,
                                                                                  AnalysisConfigurator<T> configurator,
                                                                                  String goal) {
        if (jobClass.isAssignableFrom(MavenModuleSet.class)) {
            MavenInstallation.ensureThatMavenIsInstalled(jenkins);
        }

        J job = jenkins.jobs.create(jobClass);
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
                || jobClass.isAssignableFrom((MatrixProject.class));
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
    private <J extends Job, T extends AnalysisSettings & PostBuildStep> J edit(String newResourceToCopy,
                                                                               boolean isAdditionalResource,
                                                                               J job,
                                                                               Class<T> publisherBuildSettingsClass,
                                                                               @CheckForNull AnalysisConfigurator<T> configurator) {
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

        //change the configuration of the publisher
        if (configurator != null) {

            if (job instanceof MavenModuleSet) {
                configurator.configure(((MavenModuleSet) job).getBuildSettings(publisherBuildSettingsClass));
            }
            else if (job instanceof FreeStyleJob) {
                configurator.configure(job.getPublisher(publisherBuildSettingsClass));
            }


        }

        job.save();
        return job;
    }


    /**
     * Generates a slave and configure job to run on slave
     *
     * @param job Job to run on slave
     * @return Generated slave
     * @throws ExecutionException   if computation of slave threw an exception
     * @throws InterruptedException if thread was interrupted while waiting
     */
    public Slave makeASlaveAndConfigureJob(Job job) throws ExecutionException, InterruptedException {
        Slave slave = slaveController.install(jenkins).get();
        job.configure();
        job.setLabelExpression(slave.getName());
        job.save();
        return slave;
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
     * Build Job successfully once.
     *
     * @param job Job to build
     * @return The made build
     */
    public Build buildJobWithSuccess(Job job) {
        return buildJobAndWait(job).shouldSucceed();
    }

    /**
     * Build Job and wait until finished.
     *
     * @param job   Job to build
     * @param slave Slave to run job on
     * @return The made build
     */
    public Build buildJobOnSlaveWithSuccess(FreeStyleJob job, Node slave) {
        return job.startBuild(singletonMap("slavename", slave.getName())).shouldSucceed();
    }

    /**
     * When Given a finished build, an API-Url and a reference XML-File, this method compares if the api call to the
     * build matches the expected XML-File. Whitespace differences are ignored.
     *
     * @param build           The build, whose api shall be called.
     * @param apiUrl          The API-Url, declares which build API shall be called.
     * @param expectedXmlPath The Resource-Path to a file, which contains the expected XML
     */
    protected void assertXmlApiMatchesExpected(Build build, String apiUrl, String expectedXmlPath) throws ParserConfigurationException, SAXException, IOException {
        XMLUnit.setIgnoreWhitespace(true);
        String xmlUrl = build.url(apiUrl).toString();
        DocumentBuilder documentBuilder = DocumentBuilderFactoryImpl.newInstance().newDocumentBuilder();
        Document actual = documentBuilder.parse(xmlUrl);

        Document expected = documentBuilder.parse(resource(expectedXmlPath).asFile());
        XMLAssert.assertXMLEqual(expected, actual);
    }

    /**
     * Checks if the area links of jobs matches the regular expression.
     *
     * @param job               Job to check the area links
     * @param regularExpression Expression should match
     */
    public void assertAreaLinksOfJobAreLike(Job job, String regularExpression) {
        Pattern pattern = Pattern.compile(regularExpression);
        for (String currentLink : job.getAreaLinks()) {
            assertTrue("Link should be relative", pattern.matcher(currentLink).matches());
        }
    }

    /**
     * Creates a new view and adds the given column to the view.
     *
     * @param columnClass The ListViewColumn that should bee added.
     * @param <T>         The concrete type of the ListViewColumn.
     * @return The ListView.
     */
    protected <T extends ListViewColumn> ListView addDashboardListViewColumn(Class<T> columnClass) {
        ListView view = createNewViewForAllJobs(ListView.class);
        view.addColumn(columnClass);
        view.save();
        return view;
    }

    /**
     * Creates a new view with a random name that matches all jobs.
     *
     * @param viewClass The view that shall be used.
     * @param <T>       The type constraint of the view.
     * @return The view.
     */
    private <T extends View> T createNewViewForAllJobs(Class<T> viewClass) {
        T view = jenkins.views.create(viewClass, jenkins.createRandomName());
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
     * @param portlet The Portlet that shall be added.
     * @param <T>     The type constraint of the portlet.
     * @return The view.
     */
    protected <T extends AbstractDashboardViewPortlet> DashboardView addDashboardViewAndBottomPortlet(Class<T> portlet) {
        DashboardView view = createNewViewForAllJobs(DashboardView.class);
        view.addBottomPortlet(portlet);
        view.save();
        return view;
    }
}
