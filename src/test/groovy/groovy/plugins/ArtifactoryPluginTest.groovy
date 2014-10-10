package groovy.plugins

import org.jenkinsci.test.acceptance.geb.GebSpec
import org.jenkinsci.test.acceptance.global.GlobalConfigurationPage
import org.jenkinsci.test.acceptance.junit.WithPlugins
import org.jenkinsci.test.acceptance.plugins.artifactory.ArtifactoryPublisher
import org.jenkinsci.test.acceptance.plugins.gradle.GradleStep
import org.jenkinsci.test.acceptance.plugins.maven.MavenModuleSet
import org.jenkinsci.test.acceptance.po.FreeStyleJob
import spock.lang.Ignore

import static org.jenkinsci.test.acceptance.plugins.maven.MavenInstallation.installSomeMaven

/**
 * Created by eli on 10/6/14.
 */
@WithPlugins(['artifactory', 'gradle', 'maven-plugin'])
//@Native("docker")
//@Stepwise
class ArtifactoryPluginTest extends GebSpec {

//    @Inject
//    DockerContainerHolder<ArtifactoryContainer> artifactoryServer;
    def "Check config is persistence"() {
        given:
        addArtifactory()
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

    def "Maven integration"() {
        given:
        addArtifactory()
        installSomeMaven(jenkins);
        MavenModuleSet job = jenkins.jobs.create(MavenModuleSet.class);
        job.configure();
        job.copyDir(resource("/artifactory_plugin/multimodule/"));
        job.goals.set("install");
        job.options("-verbose");
        def publisher = job.addPublisher(ArtifactoryPublisher.class)
        publisher.refreshRepoButton.click()
        job.save();

        when:
        def build = job.startBuild()

        then:
        build.shouldSucceed()

        and:
        build.shouldContainsConsoleOutput('Initializing Artifactory Build-Info Recording')

        and:
        build.shouldContainsConsoleOutput('Deploying artifact')

        and:
        build.shouldContainsConsoleOutput('Deploying build info to: http://localhost:8081/artifactory/api/build')
    }


    def "Gradle integration"() {
        given:
        addArtifactory()
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
        build.shouldContainsConsoleOutput('build artifactoryPublish')

        and:
        build.shouldContainsConsoleOutput('[buildinfo]')

        and:
        build.shouldContainsConsoleOutput('Deploying artifact')

        and:
        build.shouldContainsConsoleOutput('Deploying build info to: http://localhost:8081/artifactory/api/build')

    }

    def addArtifactory() {
        to GlobalConfigurationPage
        addArtifactoryButton.click()
        artifactoryUrl.value('http://localhost:8081/artifactory')
        artifactoryUsername.value('admin')
        artifactoryPassword.value('password')
        saveButton.click()
    }
}