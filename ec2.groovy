import org.jenkinsci.test.acceptance.PublicKeyAuthenticator
import org.jenkinsci.test.acceptance.controller.*
import org.jenkinsci.test.acceptance.guice.TestScope
import org.jenkinsci.test.acceptance.machine.Ec2Provider
import org.jenkinsci.test.acceptance.machine.JenkinsProvider
import org.jenkinsci.test.acceptance.machine.RemoteJenkinsProvider
import org.jenkinsci.test.acceptance.machine.MachineProvider
import org.jenkinsci.test.acceptance.machine.MultitenancyMachineProvider
import org.jenkinsci.test.acceptance.resolver.JenkinsDownloader
import org.jenkinsci.test.acceptance.resolver.JenkinsResolver
import org.jenkinsci.test.acceptance.slave.SlaveProvider
import org.jenkinsci.test.acceptance.slave.SshSlaveProvider

def localWar = new File("jenkins.war")

def common = module {
    bind(org.jenkinsci.test.acceptance.Authenticator).named("publicKeyAuthenticator").to(PublicKeyAuthenticator)

    jenkins_md5_sum="b6aacb5f25a323120f8c791fe2d947b9"
    bind JenkinsResolver toInstance new JenkinsDownloader("http://mirrors.jenkins-ci.org/war/latest/jenkins.war")
    // bind JenkinsResolver toInstance new JenkinsUploader(localWar)
}

slaves = subworld {
    maxMtMachines=30
    maxNumOfMachines=1
    install(common)

    user="ubuntu"

    bind MachineProvider to MultitenancyMachineProvider
    bind MachineProvider named "raw" to Ec2Provider

}

masters = subworld {
    maxMtMachines=2
    maxNumOfMachines=1
    install(common)

    user="ubuntu"

    bind MachineProvider to MultitenancyMachineProvider
    bind MachineProvider named "raw" to Ec2Provider
}

joc = subworld {
    bind JenkinsController toInstance new WinstoneController(localWar, JenkinsController.downloadPathElement())
}

bind SlaveProvider toProvider slaves[SshSlaveProvider]
bind(JenkinsProvider).named("masters").toProvider(masters[RemoteJenkinsProvider])
bind(JenkinsController).named("joc").toProvider(joc[JenkinsController]).in(TestScope)

