import org.jenkinsci.test.acceptance.controller.*
import org.jenkinsci.test.acceptance.guice.TestScope
import org.jenkinsci.test.acceptance.resolver.JenkinsDownloader
import org.jenkinsci.test.acceptance.resolver.JenkinsResolver
import org.jenkinsci.test.acceptance.slave.SlaveProvider
import org.jenkinsci.test.acceptance.slave.SshSlaveProvider

//node_id="i-9105c3b2"

def localWar = new File("jenkins.war")

max_mt_machines=2
privateKeyFile = new File(".jenkins_test/.ssh/id_rsa")
publicKeyFile = new File(".jenkins_test/.ssh/id_rsa.pub")
bind(Authenticator).named("publicKeyAuthenticator").to(PublicKeyAuthenticator)

user = "ubuntu"

jenkins_md5_sum="b6aacb5f25a323120f8c791fe2d947b9"
bind JenkinsResolver toInstance new JenkinsDownloader("http://mirrors.jenkins-ci.org/war/latest/jenkins.war")
// bind JenkinsResolver toInstance new JenkinsUploader(localWar)

bind SlaveProvider to SshSlaveProvider
bind MachineProvider to Ec2Provider
bind JenkinsController toProvider JenkinsProvider "in" TestScope

