import org.jenkinsci.test.acceptance.controller.*
import org.jenkinsci.test.acceptance.resolver.JenkinsDownloader
import org.jenkinsci.test.acceptance.resolver.JenkinsResolver
import org.jenkinsci.test.acceptance.slave.SlaveProvider
import org.jenkinsci.test.acceptance.slave.SshSlaveProvider

//node_id="i-9105c3b2"

def localWar = new File("/Users/vivek/Downloads/jenkins.war")

def common = module {
    max_mt_machines=2
    privateKeyFile = new File("/Users/vivek/ws/jenkins/acceptance-test-harness/.jenkins_test/.ssh/id_rsa")
    publicKeyFile = new File("/Users/vivek/ws/jenkins/acceptance-test-harness/.jenkins_test/.ssh/id_rsa.pub")
    bind(Authenticator).named("publicKeyAuthenticator").to(PublicKeyAuthenticator)

    jenkins_md5_sum="b6aacb5f25a323120f8c791fe2d947b9"
    bind JenkinsResolver toInstance new JenkinsDownloader("http://mirrors.jenkins-ci.org/war/latest/jenkins.war")
    // bind JenkinsResolver toInstance new JenkinsUploader(localWar)
}

def slaves = subworld("slaves") {
    install(common)

    user="ubuntu1"

    bind MachineProvider to Ec2Provider
}

subworld("masters") {
    install(common)

    user="ubuntu2"

    bind MachineProvider to Ec2Provider
}

subworld("joc") {
    bind JenkinsController toInstance new WinstoneController(localWar)
}

bind SlaveProvider toProvider slaves[SshSlaveProvider]
bind JenkinsProvider named "masters" toProvider masters[JenkinsProvider]
bind JenkinsController named "joc" toProvider joc[JenkinsController]

