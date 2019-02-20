// For ci.jenkins.io
// https://github.com/jenkins-infra/documentation/blob/master/ci.adoc

for (int i = 0; i < (BUILD_NUMBER as int); i++) {
    milestone()
}
def branches = [:]
for (int j in [8, 11]) {
    int javaVersion = j
    def splits = splitTests parallelism: count(10), estimateTestsFromFiles: (BUILD_NUMBER == 1)
    for (int i = 0; i < splits.size(); i++) {
        int index = i
        def name = "java-${javaVersion}-split${index}"
        branches[name] = {
            stage(name) {
                node('docker && highmem') {
                    checkout scm
                    def image = docker.build('jenkins/ath', "src/main/resources/ath-container")
                    image.inside('-v /var/run/docker.sock:/var/run/docker.sock --shm-size 2g') {
                        def exclusions = splits.get(index).join("\n")
                        writeFile file: 'excludes.txt', text: exclusions
                        realtimeJUnit(testResults: 'target/surefire-reports/TEST-*.xml', testDataPublishers: [[$class: 'AttachmentPublisher']]) {
                            sh """
                                set-java.sh $javaVersion
                                eval \$(vnc.sh)
                                java -version

                                echo "Excludes:"
                                cat excludes.txt

                                run.sh firefox latest -Dmaven.test.failure.ignore=true -DforkCount=1 -B
                            """
                        }
                    }
                }
            }
        }
    }
}
parallel branches
