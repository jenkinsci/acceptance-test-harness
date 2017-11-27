// For ci.jenkins.io
// https://github.com/jenkins-infra/documentation/blob/master/ci.adoc

pipeline {
    agent {
        dockerfile {
            dir 'src/main/resources/ath-container'
            args '-v /var/run/docker.sock:/var/run/docker.sock'
            label 'docker && highmem'
        }
    }
    stages {
        stage('Run ATH') {
            steps {
                script { // TODO pending proper solution for JENKINS-43353
                    for (int i = 0; i < (BUILD_NUMBER as int); i++) {milestone()}
                }
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
