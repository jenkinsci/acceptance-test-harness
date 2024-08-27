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

        find(by.href("index-all.html")).click();
        find(by.href("com/mycompany/app/package-summary.html")).click();

        assertThat(driver, hasContent("com.mycompany.app"));
    }
}
