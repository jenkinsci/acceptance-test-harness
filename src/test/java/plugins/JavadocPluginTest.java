package plugins;

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
import org.openqa.selenium.NoSuchFrameException;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.jenkinsci.test.acceptance.Matchers.*;

@WithPlugins("javadoc")
public class JavadocPluginTest extends AbstractJUnitTest {

    private static final String JAVADOC_ACTION = "Javadoc";

    @Test
    public void publish_javadoc_from_freestyle_job() {
        FreeStyleJob job = jenkins.jobs.create();
        setup(job, "single_package");

        job.startBuild().shouldSucceed();
        assertThat(job, hasAction(JAVADOC_ACTION));

        assertJavadoc(job);
    }

    @Test @WithPlugins("matrix-project")
    public void publish_javadoc_from_matrix_job() {
        MatrixProject job = jenkins.jobs.create(MatrixProject.class);
        setup(job, "multi_package");

        job.startBuild().shouldSucceed();
        MatrixConfiguration def = job.getConfiguration("default");
        assertThat(def, hasAction(JAVADOC_ACTION));

        assertJavadoc(def);
    }

    @Test
    public void validate_javadoc_retention() {
        FreeStyleJob job = jenkins.jobs.create();
        setup(job, "single_package");
        Build b = job.startBuild().shouldSucceed();

        assertThat(job, hasAction(JAVADOC_ACTION));
        assertThat("Build #1 should not have Javadoc action", b, not(hasAction(JAVADOC_ACTION)));

        this.setupForRetention(job);
        b = job.startBuild().shouldSucceed();

        assertThat(job, hasAction(JAVADOC_ACTION));
        assertThat(b, hasAction(JAVADOC_ACTION));
    }

    private void setup(Job job, String app) {
        // Not needed after https://github.com/jenkinsci/javadoc-plugin/pull/10. Reevaluate if needs to be brought back for older javadoc versions
        // https://wiki.jenkins.io/display/JENKINS/Configuring+Content+Security+Policy#ConfiguringContentSecurityPolicy-JavadocPlugin
        // jenkins.runScript("System.setProperty('hudson.model.DirectoryBrowserSupport.CSP', \"default-src 'none'; img-src 'self'; style-src 'self'; child-src 'self'; frame-src 'self';\")");

        job.configure();
        job.copyDir(resource("/javadoc_plugin/" + app));
        job.addBuildStep(MavenBuildStep.class).targets.set("javadoc:javadoc");

        JavadocPublisher jd = job.addPublisher(JavadocPublisher.class);
        jd.javadocDir.set("target/site/apidocs/");
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

        // Prior Java 9 the main content ware in frame. Also, JENKINS-32619 points directly of the main content in some cases.
        try {
            try {
                driver.switchTo().frame("classFrame");
                verify();
            } finally {
                driver.switchTo().parentFrame();
            }
        } catch (NoSuchFrameException nf) {
            // Verify directly otherwise
            verify();
        }
    }

    private void verify() {
        assertThat(driver, hasContent("com.mycompany.app"));
    }
}
