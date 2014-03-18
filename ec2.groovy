import org.jenkinsci.test.acceptance.controller.*
import org.jenkinsci.test.acceptance.guice.TestScope
import org.jenkinsci.test.acceptance.resolver.JenkinsDownloader
import org.jenkinsci.test.acceptance.resolver.JenkinsResolver
import org.jenkinsci.test.acceptance.slave.SlaveProvider
import org.jenkinsci.test.acceptance.slave.SshSlaveProvider

def localWar = new File("/Users/vivek/Downloads/jenkins.war")

def common = module {
    privateKeyFile = new File(".jenkins_test/.ssh/id_rsa")
    publicKeyFile = new File(".jenkins_test/.ssh/id_rsa.pub")
    bind(Authenticator).named("publicKeyAuthenticator").to(PublicKeyAuthenticator)

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
    bind JenkinsController toInstance new WinstoneController(localWar)
}

bind SlaveProvider toProvider slaves[SshSlaveProvider]
bind(JenkinsProvider).named("masters").toProvider(masters[JenkinsProvider])
bind(JenkinsController).named("joc").toProvider(joc[JenkinsController]).in(TestScope)

