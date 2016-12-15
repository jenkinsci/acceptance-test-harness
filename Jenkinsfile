// For ci.jenkins.io
// https://github.com/jenkins-infra/documentation/blob/master/ci.adoc

def mavenName = 'mvn'
def jdkName = 'jdk8'

node('highram&&docker') {
    withEnv([
        "JAVA_HOME=${tool jdkName}",
        "PATH+MAVEN=${tool mavenName}/bin:${env.JAVA_HOME}/bin"
    ]) {
        changelog scm

        sh 'docker build --build-arg=uid=$(id -u) --build-arg=gid=$(id -g) -t jenkins/ath src/main/resources/ath-container'
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
