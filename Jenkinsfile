// For ci.jenkins.io
// https://github.com/jenkins-infra/documentation/blob/master/ci.adoc


    for (int i = 0; i < (BUILD_NUMBER as int); i++) {
      milestone()
    }
    def splits = splitTests count(10)
    def branches = [:]
    for (int i = 0; i < splits.size(); i++) {
        int index = i;
        branches["split${i}"] = {
            stage("Run ATH - split${index}") {
                node('docker && highmem'){
                    checkout scm
                    def image = docker.build('jenkins/ath', 'src/main/resources/ath-container')
                    image.inside('-v /var/run/docker.sock:/var/run/docker.sock') { // checking
                        def exclusions = splits.get(index).join("\n");
                        writeFile file: 'excludes.txt', text: exclusions
                        realtimeJUnit(testResults: 'target/surefire-reports/TEST-*.xml', testDataPublishers: [[$class: 'AttachmentPublisher']]) {
                            sh '''
                                export SHARED_DOCKER_SERVICE=true
                                eval $(./vnc.sh)
                                ./run.sh firefox latest -Dmaven.test.failure.ignore=true -DforkCount=1 -B
                            '''
                        }
                    }
                }
            }
        }
    }
    parallel branches
