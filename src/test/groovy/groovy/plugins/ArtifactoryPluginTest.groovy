package groovy.plugins

import org.apache.commons.io.IOUtils
import org.jenkinsci.test.acceptance.docker.DockerContainerHolder
import org.jenkinsci.test.acceptance.docker.fixtures.ArtifactoryContainer
import org.jenkinsci.test.acceptance.geb.GebSpec
import org.jenkinsci.test.acceptance.global.GlobalConfigurationPage
import org.jenkinsci.test.acceptance.junit.Native
import org.jenkinsci.test.acceptance.junit.WithPlugins
import org.jenkinsci.test.acceptance.plugins.artifactory.ArtifactoryPublisher
import org.jenkinsci.test.acceptance.plugins.gradle.GradleStep
import org.jenkinsci.test.acceptance.plugins.maven.MavenModuleSet
import org.jenkinsci.test.acceptance.po.FreeStyleJob
import com.google.inject.Inject

import java.util.concurrent.Callable;

import static org.jenkinsci.test.acceptance.plugins.maven.MavenInstallation.installSomeMaven

/**
 * Checks the successfully integration of Artifactory plugin.
 *
 * @author Eli Givoni
 */
@WithPlugins(['artifactory', 'gradle', 'maven-plugin'])
@Native("docker")
class ArtifactoryPluginTest extends GebSpec {

    @Inject
    DockerContainerHolder<ArtifactoryContainer> artifactoryContainer;

    def check_config_is_persisted() {
        given:
        final ArtifactoryContainer artifactory = artifactoryContainer.get()
        waitForArtifactory(artifactory)
        addArtifactory(artifactory)
        to GlobalConfigurationPage

        expect:
        at GlobalConfigurationPage

        when:
        artifactoryTestConnectionButton.click()

        then:
        goodConnectionFeedback.text().contains('Found Artifactory')

        when: "Check wrong credentials"
        artifactoryUsername.value('bob')
        artifactoryTestConnectionButton.click()

        then:
        errorConnectionFeedback.text() == 'Error occurred while requesting version information: Unauthorized'

        when: "Check wrong url"
        artifactoryUrl.value('http://localhost:4898/blabla')
        artifactoryTestConnectionButton.click()

        then:
        errorConnectionFeedback.text().contains('Connection to http://localhost:4898 refused')
    }

    def maven_integration() {
        given:
        installSomeMaven(jenkins);
        final ArtifactoryContainer artifactory = artifactoryContainer.get()
        waitForArtifactory(artifactory)
        addArtifactory(artifactory)
        MavenModuleSet job = jenkins.jobs.create(MavenModuleSet.class);
        job.configure();
        job.copyDir(resource("/artifactory_plugin/multimodule/"));
        job.goals.set("install");
        job.options("-verbose");
        def publisher = job.addPublisher(ArtifactoryPublisher.class)
        publisher.refreshRepoButton.click()
        waitFor { publisher.targetReleaseRepository.text() != "" }
        job.save();

        when:
        def build = job.startBuild()

        then:
        build.shouldSucceed()

        and:
        def log = build.console
        assertThat(log, containsString('Initializing Artifactory Build-Info Recording'))

        and:
        assertThat(log, containsString('Deploying artifact'))

        and:
        assertThat(log, containsString("Deploying build info to: ${artifactory.getURL()}/api/build"))
    }

    def gradle_integration() {
        given:
        final ArtifactoryContainer artifactory = artifactoryContainer.get()
        waitForArtifactory(artifactory)
        addArtifactory(artifactory)
        FreeStyleJob job = jenkins.jobs.create();
        job.copyDir(resource('/artifactory_plugin/quickstart'))
        $('input', name: 'org-jfrog-hudson-gradle-ArtifactoryGradleConfigurator').click()
        $('button', path: '/org-jfrog-hudson-gradle-ArtifactoryGradleConfigurator/details/validate-button').click()
        GradleStep step = job.addBuildStep(GradleStep.class)
        $('input', path: '/builder[1]/useWrapper[true]').click()
        $('input', path: '/builder[1]/makeExecutable').click()
        step.tasks.set("build");
        job.save();

        when:
        def build = job.startBuild()

        then:
        build.shouldSucceed()

        and:
        def log = build.console
        assertThat(log, containsString('build artifactoryPublish'))

        and:
        assertThat(log, containsString('[buildinfo]'))

        and:
        assertThat(log, containsString('Deploying artifact'))

        and:
        assertThat(log, containsString("Deploying build info to: ${artifactory.getURL()}/api/build"))
    }

    def addArtifactory(ArtifactoryContainer artifactory) {
        to GlobalConfigurationPage
        addArtifactoryButton.click()
        artifactoryUrl.value(artifactory.getURL())
        artifactoryUsername.value('admin')
        artifactoryPassword.value('password')
        saveButton.click()
    }

    def waitForArtifactory(ArtifactoryContainer artifactory) {
        jenkins.waitFor().withMessage("Artifactory is up").until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                try {
                    String s = IOUtils.toString(artifactory.getPingURL().openStream());
                    return s.contains("OK");
                } catch (IOException e) {//catching IOException when server in not fully up and retuning 503
                    return null;
                }
            }
        });
    }
}