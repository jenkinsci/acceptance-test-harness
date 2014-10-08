package groovy.plugins

import org.jenkinsci.test.acceptance.docker.DockerContainerHolder
import org.jenkinsci.test.acceptance.docker.fixtures.ArtifactoryContainer
import org.jenkinsci.test.acceptance.geb.GebSpec
import org.jenkinsci.test.acceptance.global.GlobalConfigurationPage
import org.jenkinsci.test.acceptance.junit.WithPlugins
import spock.lang.Stepwise

import javax.inject.Inject

/**
 * Created by eli on 10/6/14.
 */
@WithPlugins(['artifactory'])
//@Native("docker")
@Stepwise
class ArtifactoryPluginTest extends GebSpec {

//    @Inject
//    DockerContainerHolder<ArtifactoryContainer> artifactoryServer;
    def "Check config is persistence"() {
        given:
        to GlobalConfigurationPage

        expect:
        at GlobalConfigurationPage

        when:
        addArtifactoryButton.click()
        artifactoryUrl.value('http://localhost:8081/artifactory')
        artifactoryUsername.value('admin')
        artifactoryPassword.value('password')
        applyButton.click()
        artifactoryTestConnectionButton.click()

        then:
        $('div', class: 'ok').text().contains('Found Artifactory')

        when:"Check wrong credentials"
        artifactoryUsername.value('bob')
        artifactoryTestConnectionButton.click()

        then:
        $('div', class: 'error').text() == 'Error occurred while requesting version information: Unauthorized'

        when:"Check wrong url"
        artifactoryUrl.value('http://localhost:4898/blabla')
        artifactoryTestConnectionButton.click()

        then:
        $('div', class: 'error').text().contains('Connection to http://localhost:4898 refused')

    }
}