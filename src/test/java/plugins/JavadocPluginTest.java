package plugins;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.javadoc.JavadocPublisher;
import org.jenkinsci.test.acceptance.plugins.maven.MavenBuildStep;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.Job;
import org.jenkinsci.test.acceptance.po.MatrixConfiguration;
import org.jenkinsci.test.acceptance.po.MatrixProject;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.jenkinsci.test.acceptance.Matchers.hasAction;
import static org.jenkinsci.test.acceptance.Matchers.hasContent;

@WithPlugins("javadoc")
public class JavadocPluginTest extends AbstractJUnitTest {

    @Test
    public void publish_javadoc_from_freestyle_job() {
        FreeStyleJob job = jenkins.jobs.create();
        setup(job);

        job.startBuild().shouldSucceed();
        assertThat(job, hasAction("Javadoc"));

        assertJavadoc(job);
    }

    @Test
    public void publish_javadoc_from_matrix_job() {
        MatrixProject job = jenkins.jobs.create(MatrixProject.class);
        setup(job);

        job.startBuild().shouldSucceed();
        MatrixConfiguration def = job.getConfiguration("default");
        assertThat(def, hasAction("Javadoc"));

        assertJavadoc(def);
    }

    private void setup(Job job) {
        job.configure();
        MavenBuildStep m = job.addBuildStep(MavenBuildStep.class);
        m.targets.set("archetype:generate -DarchetypeGroupId=org.apache.maven.archetypes -DgroupId=com.mycompany.app -DartifactId=my-app -Dversion=1.0 -B");
        m = job.addBuildStep(MavenBuildStep.class);
        m.targets.set("javadoc:javadoc -f my-app/pom.xml");

        JavadocPublisher jd = job.addPublisher(JavadocPublisher.class);
        jd.javadocDir.set("my-app/target/site/apidocs/");
        job.save();
    }

    private void assertJavadoc(Job job) {
        job.open();
        find(by.link("Javadoc")).click();
        driver.switchTo().frame("classFrame");
        assertThat(driver, hasContent("com.mycompany.app"));
    }
}
