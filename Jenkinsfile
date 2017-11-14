// For ci.jenkins.io
// https://github.com/jenkins-infra/documentation/blob/master/ci.adoc

    def splits = splitTests count(10)
    def branches = [:]
    for (int i = 0; i < splits.size(); i++) {
        int index = i;
        branches["split${i}"] = {
            stage("Run ATH - split${index}") {
                node('docker && highmem'){
                    checkout scm
                    def image = docker.build('jenkins/ath', 'src/main/resources/ath-container')
                    image.inside('-v /var/run/docker.sock:/var/run/docker.sock') {
                        def exclusions = splits.get(index).join("\n");
                        writeFile file: 'excludes.txt', text: exclusions
                        sh '''
                            eval $(./vnc.sh)
                            ./run.sh firefox latest -Dmaven.test.failure.ignore=true -DforkCount=1 -B
                        '''
                    }
                    junit 'target/surefire-reports/TEST-*.xml'
                }
            }
        }
    }
    parallel branches

