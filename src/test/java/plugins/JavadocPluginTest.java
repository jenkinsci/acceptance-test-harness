package plugins;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.jenkinsci.test.acceptance.Matchers.hasAction;
import static org.jenkinsci.test.acceptance.Matchers.hasContent;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.javadoc.JavadocPublisher;
import org.jenkinsci.test.acceptance.plugins.maven.MavenBuildStep;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.Job;
import org.jenkinsci.test.acceptance.po.MatrixConfiguration;
import org.jenkinsci.test.acceptance.po.MatrixProject;
import org.junit.Test;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.NoSuchFrameException;

@WithPlugins("javadoc")
public class JavadocPluginTest extends AbstractJUnitTest {

    private static final String JAVADOC_ACTION = "Javadoc";

    @Test
    public void publish_javadoc_from_freestyle_job() {
        FreeStyleJob job = jenkins.jobs.create();
        setup(job);

        job.startBuild().shouldSucceed();
        assertThat(job, hasAction(JAVADOC_ACTION));

        assertJavadoc(job);
    }

    @Test @WithPlugins("matrix-project")
    public void publish_javadoc_from_matrix_job() {
        MatrixProject job = jenkins.jobs.create(MatrixProject.class);
        setup(job);

        job.startBuild().shouldSucceed();
        MatrixConfiguration def = job.getConfiguration("default");
        assertThat(def, hasAction(JAVADOC_ACTION));

        assertJavadoc(def);
    }

    @Test
    public void validate_javadoc_retention() {
        FreeStyleJob job = jenkins.jobs.create();
        setup(job);
        Build b = job.startBuild().shouldSucceed();

        assertThat(job, hasAction(JAVADOC_ACTION));
        assertThat("Build #1 should not have Javadoc action", b, not(hasAction(JAVADOC_ACTION)));

        this.setupForRetention(job);
        b = job.startBuild().shouldSucceed();

        assertThat(job, hasAction(JAVADOC_ACTION));
        assertThat(b, hasAction(JAVADOC_ACTION));
    }

    private void setup(Job job) {
        // https://wiki.jenkins.io/display/JENKINS/Configuring+Content+Security+Policy#ConfiguringContentSecurityPolicy-JavadocPlugin
        jenkins.runScript("System.setProperty('hudson.model.DirectoryBrowserSupport.CSP', \"default-src 'none'; img-src 'self'; style-src 'self'; child-src 'self'; frame-src 'self';\")");

        job.configure();
        MavenBuildStep m = job.addBuildStep(MavenBuildStep.class);
        m.targets.set("archetype:generate -DarchetypeGroupId=org.apache.maven.archetypes -DgroupId=com.mycompany.app -DartifactId=my-app -Dversion=1.0 -B");
        m = job.addBuildStep(MavenBuildStep.class);
        m.targets.set("javadoc:javadoc -f my-app/pom.xml");

        JavadocPublisher jd = job.addPublisher(JavadocPublisher.class);
        jd.javadocDir.set("my-app/target/site/apidocs/");
        job.save();
    }

    private void setupForRetention(Job job) {
        job.editPublisher(JavadocPublisher.class, (publisher) -> {
            job.removeFirstBuildStep();
            publisher.keepAll.check();
        });
    }

    private void assertJavadoc(Job job) {
        job.open();
        find(by.link(JAVADOC_ACTION)).click();

        /*
        Why do we check in such way?

        The java version of the Jenkins under test could be different that the one in use by this code, the ATH code.
        See: https://github.com/jenkinsci/acceptance-test-harness/blob/39cbea43b73d32a0912d613a41e08ba9b54aad1d/src/main/java/org/jenkinsci/test/acceptance/controller/LocalController.java#L190

        In addition, the way javadoc is generated depends on the java version and the number of packages generated, one
        or more packages.
        See: https://issues.jenkins-ci.org/browse/JENKINS-32619?focusedCommentId=311819&page=com.atlassian.jira.plugin.system.issuetabpanels%3Acomment-tabpanel#comment-311819

        So even though it's not the most refined way to check the right generation of the javadoc, this way doesn't
        need to call a groovy script for checking the java version neither checking the number of packages generated.
        */

         try {
             // Former behavior, javadoc generating frames and plugin without redirection
             driver.switchTo().frame("classFrame");
         } catch (NoSuchFrameException e) {
             try {
                 // With Java11 a link to the package-summary is shown, no frames
                 find(by.partialLinkText("package-summary.html")).click();
             } catch (NoSuchElementException ignored) {
             }
         }

         assertThat(driver, hasContent("com.mycompany.app"));
    }
}
