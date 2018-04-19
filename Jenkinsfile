// For ci.jenkins.io
// https://github.com/jenkins-infra/documentation/blob/master/ci.adoc

def imageName = 'jenkinsciinfra/ath'

def container
/* Assuming we're not inside of a pull request or multibranch pipeline */
if (!(env.CHANGE_ID || env.BRANCH_NAME)) {
    node('docker') {
        stage('Prepare Container') {
            timestamps {
                checkout scm
                sh 'git rev-parse HEAD > GIT_COMMIT'
                shortCommit = readFile('GIT_COMMIT').take(6)
                def imageTag = "${env.BUILD_ID}-build${shortCommit}"
                echo "Creating the container ${imageName}:${imageTag}"
                container = docker.build("${imageName}:${imageTag}", 'src/main/resources/ath-container')
            }
        }

        stage('Publish container') {
            infra.withDockerCredentials {
                timestamps { container.push() }
            }
        }
    }
} else { // PR or multibranch pipeline

    for (int i = 0; i < (BUILD_NUMBER as int); i++) {
        milestone()
    }
    def splits = splitTests count(10)
    def branches = [:]
    for (int i = 0; i < splits.size(); i++) {
        int index = i;
        branches["split${i}"] = {
            stage("Run ATH - split${index}") {
                node('docker && highmem') {
                    checkout scm
                    def image = docker.build('jenkins/ath', 'src/main/resources/ath-container')
                    image.inside('-v /var/run/docker.sock:/var/run/docker.sock') {
                        def exclusions = splits.get(index).join("\n");
                        writeFile file: 'excludes.txt', text: exclusions
                        realtimeJUnit(testResults: 'target/surefire-reports/TEST-*.xml', testDataPublishers: [[$class: 'AttachmentPublisher']]) {
                            sh '''
                                export SHARED_DOCKER_SERVICE=true
                                eval $(./vnc.sh)
                                export DISPLAY=$BROWSER_DISPLAY # No need to use separate variable in automation
                                ./run.sh firefox latest -Dmaven.test.failure.ignore=true -DforkCount=1 -B
                            '''
                        }
                    }
                }
            }
        }
    }
    parallel branches
}