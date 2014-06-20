package plugins;

import com.google.inject.Inject;
import org.apache.xerces.jaxp.DocumentBuilderFactoryImpl;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.Resource;
import org.jenkinsci.test.acceptance.plugins.AbstractCodeStylePluginBuildConfigurator;
import org.jenkinsci.test.acceptance.plugins.AbstractCodeStylePluginBuildSettings;
import org.jenkinsci.test.acceptance.plugins.AbstractCodeStylePluginFreestyleBuildSettings;
import org.jenkinsci.test.acceptance.plugins.AbstractCodeStylePluginMavenBuildSettings;
import org.jenkinsci.test.acceptance.plugins.maven.MavenBuildStep;
import org.jenkinsci.test.acceptance.plugins.maven.MavenInstallation;
import org.jenkinsci.test.acceptance.plugins.maven.MavenModuleSet;
import org.jenkinsci.test.acceptance.po.*;
import org.jenkinsci.test.acceptance.slave.SlaveController;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

import static java.util.Collections.singletonMap;
import static org.junit.Assert.assertTrue;

public abstract class AbstractCodeStylePluginHelper extends AbstractJUnitTest {

    /** For slave test */
    @Inject
    SlaveController slaveController;

    /**
     * Setup a job with the given resource and publisher.
     * @param resourceToCopy Resource to copy to build (Directory or File path)
     * @param publisherPattern Publisher pattern to set
     * @param publisherClass Publisher to add
     * @return The made job
     */
    @Deprecated
    public <T extends AbstractCodeStylePluginFreestyleBuildSettings> FreeStyleJob setupFreestyleJob(String resourceToCopy, String publisherPattern, Class<T> publisherClass) {
        return setupFreestyleJob(resourceToCopy, publisherPattern, publisherClass, null);
    }

    /**
     * Setup a job with the given resource and publisher.
     * @param resourceToCopy Resource to copy to build (Directory or File path)
     * @param publisherClass Publisher to add
     * @param publisherPattern Publisher pattern to set
     * @return The made job
     */
    @Deprecated
    public <T extends AbstractCodeStylePluginFreestyleBuildSettings> FreeStyleJob setupFreestyleJob(String resourceToCopy,
                                                                                                    String publisherPattern,
                                                                                                    Class<T> publisherClass,
                                                                                                    AbstractCodeStylePluginBuildConfigurator<T> configurator) {
        FreeStyleJob job = jenkins.jobs.create();
        job.configure();

        Resource res = resource(resourceToCopy);
        //decide whether to utilize copyResource or copyDir
        if (res.asFile().isDirectory()) {
            job.copyDir(res);
        } else {
            job.copyResource(res);
        }

        T buildSettings = job.addPublisher(publisherClass);
        buildSettings.pattern.set(publisherPattern);

        if (configurator != null) {
            configurator.configure(buildSettings);
        }

        job.save();
        return job;
    }

    /**
     * Set up a Job of a certain type with a given resource and  a publisher which can be
     * configured by providing a configurator
     *
     * @param resourceToCopy Resource to copy to build (Directory or File path)
     * @param jobClass the type the job shall be created of, e.g. FreeStyleJob
     * @param goal a maven goal to be added to the job or null otherwise
     * @param publisherClass the type of the publisher to be added
     * @param configurator the configuration of the publisher
     * @return the new job
     */
    public <J extends Job, T extends AbstractCodeStylePluginBuildSettings & PostBuildStep> J setupJob(String resourceToCopy,
                                                                                                      Class<J> jobClass,
                                                                                                      String goal,
                                                                                                      Class<T> publisherClass,
                                                                                                      AbstractCodeStylePluginBuildConfigurator<T> configurator){
        if(jobClass.isAssignableFrom(MavenModuleSet.class)){
            MavenInstallation.ensureThatMavenIsInstalled(jenkins);
        }

        J job = jenkins.jobs.create(jobClass);
        job.configure();

        // first copy resource and then add a goal
        addResourceToJob(job, resourceToCopy);

        // check if a goal is defined and configure the job depending on the job class
        if (goal != null)
        {
            if (jobClass.isAssignableFrom(MavenModuleSet.class)){
                ((MavenModuleSet) job).goals.set(goal);
            }else if(jobClass.isAssignableFrom(FreeStyleJob.class)){
                job.addBuildStep(MavenBuildStep.class).targets.set(goal);
            }

        }

        T buildSettings = job.addPublisher(publisherClass);

        if (configurator != null) {
            configurator.configure(buildSettings);
        }

        job.save();
        return job;

    }

    /**
     * Provides the ability to edit an existing job by changing or adding the resource to copy
     * and/or by changing the configuration of a publisher
     *
     * @param newResourceToCopy the new resource to be copied to build (Directory or File path) or null if not to be changed
     * @param isAdditionalResource decides whether the old resource is kept (FALSE) or deleted (TRUE)
     * @param job the job to be changed
     * @param publisherClass the type of the publisher to be modified
     * @param configurator the new configuration of the publisher
     * @return the edited job
     */

    public <J extends Job, T extends AbstractCodeStylePluginBuildSettings & PostBuildStep> J editJob(String newResourceToCopy,
                                                                                                     boolean isAdditionalResource,
                                                                                                     J job,
                                                                                                     Class<T> publisherClass,
                                                                                                     AbstractCodeStylePluginBuildConfigurator<T> configurator){
        job.configure();

        if( newResourceToCopy != null) {
           //check whether to exchange the copy resource shell step
            if (!isAdditionalResource){
                job.removeFirstBuildStep();
            }

            //add the new copy resource shell step
            addResourceToJob(job, newResourceToCopy);
        }

        //change the configuration of the publisher
        if(configurator != null){
            configurator.configure(job.getPublisher(publisherClass));
        }

        job.save();

        return job;
    }


    /**
     * Generates a slave and configure job to run on slave
     * @param job Job to run on slave
     * @return Generated skave
     * @throws ExecutionException if computation of slave threw an exception
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
     * Setup a freestyle build with maven goals.
     * @param resourceProjectDir A Folder in resources which shall be copied to the working directory.
     * @param goal The maven goals to set.
     * @param publisherClass Publisher to add
     * @param publisherPattern Publisher pattern to set
     * @return The configured job.
     */
    @Deprecated
    public <T extends AbstractCodeStylePluginFreestyleBuildSettings> FreeStyleJob setupFreestyleJobWithMavenGoals(String resourceProjectDir, String goal, Class<T> publisherClass, String publisherPattern) {
        MavenInstallation.ensureThatMavenIsInstalled(jenkins);

        FreeStyleJob job = jenkins.jobs.create(FreeStyleJob.class);
        job.copyDir(resource(resourceProjectDir));
        job.addBuildStep(MavenBuildStep.class).targets.set(goal);

        T publisher = job.addPublisher(publisherClass);
        publisher.pattern.set(publisherPattern);

        job.save();
        return job;
    }

    /**
     * Setup a maven build.
     * @param resourceProjectDir A Folder in resources which shall be copied to the working directory. Should contain the pom.xml
     * @param goal The maven goals to set.
     * @param codeStyleBuildSettings The code analyzer to use or null if you do not want one.
     * @param configurator A configurator to custommize the code analyzer settings you want to use.
     * @param <T> The type of the Analyzer.
     * @return The configured job.
     */
    @Deprecated
    public <T extends AbstractCodeStylePluginMavenBuildSettings> MavenModuleSet setupMavenJob(String resourceProjectDir,
                                                                                              String goal,
                                                                                              Class<T> codeStyleBuildSettings,
                                                                                              AbstractCodeStylePluginBuildConfigurator<T> configurator) {
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
     * Edits a job with the given resource and publisherPattern
     * @param job Job to edit
     * @param newResourceToCopy Second resource to copy to differ the result
     * @param publisherPattern Publisher pattern to set
     * @param <T> Type of the publisher
     * @return The made job
     */
    @Deprecated
    public <T extends AbstractCodeStylePluginFreestyleBuildSettings> FreeStyleJob editJobAndChangeLastResource(FreeStyleJob job, String newResourceToCopy, String publisherPattern) {
        job.configure();
        job.removeFirstBuildStep();
        job.copyResource(resource(newResourceToCopy), publisherPattern);
        job.save();
        return job;
    }

    /**
     *  Build Job and wait until finished.
     *  @param job Job to build
     *  @return The made build
     */
    public Build buildJobAndWait(Job job) {
        return job.startBuild().waitUntilFinished();
    }

    /**
     *  Build Job successfully once.
     *  @param job Job to build
     *  @return The made build
     */
    public Build buildJobWithSuccess(Job job) {
        return buildJobAndWait(job).shouldSucceed();
    }

    /**
     *  Build Job and wait until finished.
     *  @param job Job to build
     *  @param slave Slave to run job on
     *  @return The made build
     */
    public Build buildJobOnSlaveWithSuccess(FreeStyleJob job, Slave slave) {
        return job.startBuild(singletonMap("slavename", slave.getName())).shouldSucceed();
    }

    /**
     * When Given a finished build, an API-Url and a reference XML-File, this method compares if the api call to the
     * build matches the expected XML-File. Whitespace differences are ignored.
     * @param build The build, whose api shall be called.
     * @param apiUrl The API-Url, declares which build API shall be called.
     * @param expectedXmlPath The Resource-Path to a file, which contains the expected XML
     */
    protected void assertXmlApiMatchesExpected(Build build, String apiUrl, String expectedXmlPath) throws ParserConfigurationException, SAXException, IOException {
        XMLUnit.setIgnoreWhitespace(true);
        String xmlUrl = build.url(apiUrl).toString();
        DocumentBuilder documentBuilder = DocumentBuilderFactoryImpl.newInstance().newDocumentBuilder();
        Document result = documentBuilder.parse(xmlUrl);

        Document expected = documentBuilder.parse(resource(expectedXmlPath).asFile());
        XMLAssert.assertXMLEqual(result, expected);
    }

    /**
     * Checks if the area links of jobs matches the regular expression.
     * @param job Job to check the area links
     * @param regularExpression Expression should match
     */
    public void assertAreaLinksOfJobAreLike(Job job, String regularExpression) {
        Pattern pattern = Pattern.compile(regularExpression);
        for (String currentLink : job.getAreaLinks()) {
            assertTrue("Link should be relative", pattern.matcher(currentLink).matches());
        }
    }

    /**
     * Adds a column on the dashboard.
     * @param columnClass The ListViewColumn that should bee added.
     * @param <T> The concrete type of the ListViewColumn.
     * @return The ListView.
     */
    protected <T extends ListViewColumn> ListView addDashboardColumn(Class<T> columnClass) {
        ListView view = jenkins.views.create(ListView.class, jenkins.createRandomName());
        view.configure();
        view.matchAllJobs();
        view.addColumn(columnClass);
        view.save();
        return view;
    }

    /**
     * Adds a shell step to a given job to copy resources to the job's workspace.
     *
     * @param job the job the resource shall be added to
     * @param resourceToCopy Resource to copy to build (Directory or File path)
     */
    protected <J extends Job> J addResourceToJob(J job, String resourceToCopy){

        Resource res = resource(resourceToCopy);
        //decide whether to utilize copyResource or copyDir
        if (res.asFile().isDirectory()) {
            job.copyDir(res);
        } else {
            job.copyResource(res);
        }

        return job;
    }
}
