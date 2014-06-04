package plugins;

import com.google.inject.Inject;
import org.apache.xerces.jaxp.DocumentBuilderFactoryImpl;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.Resource;
import org.jenkinsci.test.acceptance.plugins.AbstractCodeStylePluginPostBuildStep;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.Job;
import org.jenkinsci.test.acceptance.po.Slave;
import org.jenkinsci.test.acceptance.slave.SlaveController;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

import static java.util.Collections.singletonMap;

public abstract class AbstractCodeStylePluginHelper extends AbstractJUnitTest {

    /** For slave test */
    @Inject
    SlaveController slaveController;

    /**
     * Setup a job with the given resource and publisher.
     * @param resourceToCopy Resource to copy to to build
     * @param publisherClass Publisher to add
     * @param publisherPattern Publisher pattern to set
     * @param <T> Type of the publisher
     * @return The made job
     */
    public <T extends AbstractCodeStylePluginPostBuildStep> FreeStyleJob setupJob(String resourceToCopy, Class<T> publisherClass, String publisherPattern) {
        return setupJob(resourceToCopy, publisherClass, publisherPattern, null, null, false);
    }

    /**
     * Setup a job with the given resource and publisher.
     * @param resourceToCopy Resource to copy to to build
     * @param publisherClass Publisher to add
     * @param publisherPattern Publisher pattern to set
     * @param warningThresholdUnstable number of warnings needed to mark the build as unstable
     * @param thresholdFailedNewWarnings number of new warnings needed to mark the build as failure
     * @param useDeltaWarnings
     * @return The made job
     */
    public <T extends AbstractCodeStylePluginPostBuildStep> FreeStyleJob setupJob(String resourceToCopy, Class<T> publisherClass,
                                                                                  String publisherPattern, String warningThresholdUnstable,
                                                                                  String thresholdFailedNewWarnings, boolean useDeltaWarnings) {
        final FreeStyleJob job = jenkins.jobs.create();
        job.configure();

        final Resource res = resource(resourceToCopy);
        //decide whether to utilize copyResource or copyDir
        if (res.asFile().isDirectory())
            job.copyDir(res);
        else
            job.copyResource(res);

        final T publisher = job.addPublisher(publisherClass);
        publisher.pattern.set(publisherPattern);

        if (warningThresholdUnstable != null || thresholdFailedNewWarnings != null) {
            publisher.advanced.click();

            if (warningThresholdUnstable != null) {
                publisher.warningThresholdUnstable.set(warningThresholdUnstable);
            }

            if (thresholdFailedNewWarnings != null) {
                publisher.computeNewWarningsComparedWithReferenceBuild.check();
                publisher.newWarningsThresholdFailed.set(thresholdFailedNewWarnings);
            }

            if (useDeltaWarnings) {
                publisher.useDeltaValues.check();
            }
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
     * Edits a job with the given resource and publisherPattern
     * @param job Job to edit
     * @param newResourceToCopy Second resource to copy to differ the result
     * @param publisherPattern Publisher pattern to set
     * @param <T> Type of the publisher
     * @return The made job
     */
    public <T extends AbstractCodeStylePluginPostBuildStep> FreeStyleJob editJobAndChangeLastRessource(FreeStyleJob job, String newResourceToCopy, String publisherPattern) {
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
    public Build buildJobAndWait(FreeStyleJob job) {
        return job.startBuild().waitUntilFinished();
    }

    /**
     *  Build Job successfully once.
     *  @param job Job to build
     *  @return The made build
     */
    public Build buildJobWithSuccess(FreeStyleJob job) {
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
        final String xmlUrl = build.url(apiUrl).toString();
        final DocumentBuilder documentBuilder = DocumentBuilderFactoryImpl.newInstance().newDocumentBuilder();
        final Document result = documentBuilder.parse(xmlUrl);

        final Document expected = documentBuilder.parse(resource(expectedXmlPath).asFile());
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

}