// For ci.jenkins.io
// https://github.com/jenkins-infra/documentation/blob/master/ci.adoc

def mavenName = 'mvn'
def jdkName = 'jdk8'

node('highram') {
    withEnv([
        "JAVA_HOME=${tool jdkName}",
        "PATH+MAVEN=${tool mavenName}/bin:${env.JAVA_HOME}/bin"
    ]) {
        changelog scm

        def uid = sh returnStdout: true, script: "id -u | tr -d '\n'"
        def gid = sh returnStdout: true, script: "id -g | tr -d '\n'"

        def buildArgs = "--build-arg=uid=${uid} --build-arg=gid=${gid} src/main/resources/ath-container"
        docker.build('jenkins/ath', buildArgs)

        String containerArgs = '-v /var/run/docker.sock:/var/run/docker.sock'
        docker.image('jenkins/ath').inside(containerArgs) {
            sh '''
                eval $(./vnc.sh)
                ./run.sh firefox latest -Dmaven.test.failure.ignore=true -DforkCount=1 -B
            '''
        }

        junit 'target/surefire-reports/TEST-*.xml'
    }
}
