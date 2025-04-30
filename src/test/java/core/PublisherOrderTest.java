package core;

import org.apache.commons.lang3.SystemUtils;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.po.AggregateDownstreamTestResults;
import org.jenkinsci.test.acceptance.po.ArtifactArchiver;
import org.jenkinsci.test.acceptance.po.BuildTrigger;
import org.jenkinsci.test.acceptance.po.Fingerprint;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.JUnitPublisher;
import org.junit.Test;
import org.openqa.selenium.By;

@WithPlugins("junit")
public class PublisherOrderTest extends AbstractJUnitTest {

    @Test
    public void testOrdered() {
        FreeStyleJob upstream = jenkins.jobs.create(FreeStyleJob.class);
        upstream.configure();
        String command = "echo 'hello' > aggregate.txt";
        if (SystemUtils.IS_OS_UNIX) {
            upstream.addShellStep(command);
        } else {
            upstream.addBatchStep(command);
        }
        AggregateDownstreamTestResults aggregate = upstream.addPublisher(AggregateDownstreamTestResults.class);
        aggregate.specify.check();
        ArtifactArchiver archiver = upstream.addPublisher(ArtifactArchiver.class);
        archiver.includes("aggregate.txt");
        BuildTrigger trigger = upstream.addPublisher(BuildTrigger.class);
        trigger.childProjects.set("projectName");
        Fingerprint fingerprint = upstream.addPublisher(Fingerprint.class);
        fingerprint.targets.set("aggregate.txt");
        upstream.save();
    }

    @Test
    public void testUnordered() {
        FreeStyleJob upstream = jenkins.jobs.create(FreeStyleJob.class);
        upstream.configure();
        String command = "echo 'hello' > aggregate.txt";
        if (SystemUtils.IS_OS_UNIX) {
            upstream.addShellStep(command);
        } else {
            upstream.addBatchStep(command);
        }
        ArtifactArchiver archiver = upstream.addPublisher(ArtifactArchiver.class);
        archiver.includes("aggregate.txt");
        BuildTrigger trigger = upstream.addPublisher(BuildTrigger.class);
        trigger.childProjects.set("projectName");
        Fingerprint fingerprint = upstream.addPublisher(Fingerprint.class);
        fingerprint.targets.set("aggregate.txt");
        AggregateDownstreamTestResults aggregate = upstream.addPublisher(AggregateDownstreamTestResults.class);
        aggregate.specify.check();
        fingerprint.targets.set("another.txt");
        upstream.save();
        upstream.configure();
        archiver.includes("another.txt");
        JUnitPublisher junit = upstream.addPublisher(JUnitPublisher.class);
        fingerprint.targets.set("yetanother");

        /*
         * Navigate back to the dashboard first to dismiss the alert so that CspRule can check for violations (see
         * FormValidationTest).
         */
        jenkins.runThenConfirmAlert(
                () -> driver.findElement(By.xpath("//a[@href='/']")).click());
        sleep(1000);
    }
}
