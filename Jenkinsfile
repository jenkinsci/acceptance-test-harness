// For ci.jenkins.io
// https://github.com/jenkins-infra/documentation/blob/master/ci.adoc

def mavenName = 'mvn'
def jdkName = 'jdk8'

node('highram') {
    changelog scm

    def uid = sh returnStdout: true, script: "id -u | tr -d '\n'"
    def gid = sh returnStdout: true, script: "id -g | tr -d '\n'"

    def buildArgs = "--build-arg=uid=${uid} --build-arg=gid=${gid} src/main/resources/ath-container"
    def image = docker.build('jenkins/ath', buildArgs)

    String containerArgs = '-v /var/run/docker.sock:/var/run/docker.sock'
    image.inside(containerArgs) {
        sh '''
            eval $(./vnc.sh)
            ./run.sh firefox latest -Dmaven.test.failure.ignore=true -DforkCount=1 -B
        '''
    }

    junit 'target/surefire-reports/TEST-*.xml'
}
