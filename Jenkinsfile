// For ci.jenkins.io
// https://github.com/jenkins-infra/documentation/blob/master/ci.adoc

for (int i = 0; i < (BUILD_NUMBER as int); i++) {
    milestone()
}
def branches = [:]
for (javaVersion in [8, 11]) {
    def splits = splitTests count(10)
    for (int i = 0; i < splits.size(); i++) {
        int index = i
        branches["split${i}"] = {
            stage("Run ATH on java ${javaVersion} - split${index}") {
                node('docker && highmem') {
                    checkout scm
                    def image = docker.build('jenkins/ath', '--build-arg=java_version=$javaVersion src/main/resources/ath-container')
                    image.inside('-v /var/run/docker.sock:/var/run/docker.sock --shm-size 2g') {
                        def exclusions = splits.get(index).join("\n");
                        writeFile file: 'excludes.txt', text: exclusions
                        realtimeJUnit(testResults: 'target/surefire-reports/TEST-*.xml', testDataPublishers: [[$class: 'AttachmentPublisher']]) {
                            sh '''
                                eval $(./vnc.sh)
                                ./run.sh firefox latest -Dmaven.test.failure.ignore=true -DforkCount=1 -B
                            '''
                        }
                    }
                }
            }
        }
    }
}
parallel branches
