// For ci.jenkins.io
// https://github.com/jenkins-infra/documentation/blob/master/ci.adoc

for (int i = 0; i < (BUILD_NUMBER as int); i++) {
    milestone()
}
def branches = [:]
for (int j in [8, 11]) {
    int javaVersion = j
    def splits = splitTests count(10)
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
                            def java11mods = (javaVersion == 11) ? '''
                                # Temporary to get Java 11 going: https://github.com/jenkinsci/workflow-support-plugin/pull/68#issuecomment-440971292 
                                export JENKINS_OPTS="--enable-future-java"
                                export VERSION_OVERRIDES="workflow-support=3.0-java11-alpha-1"
                                export JAVA_OPTS="-p /home/ath-user/jdk11-libs/jaxb-api.jar:/home/ath-user/jdk11-libs/javax.activation.jar --add-modules java.xml.bind,java.activation -cp /home/ath-user/jdk11-libs/jaxb-impl.jar:/home/ath-user/jdk11-libs/jaxb-core.jar"
                            ''' : '';

                            sh """
                                ./set-java.sh $javaVersion
                                eval \$(./vnc.sh)
                                java -version
                                ${java11mods}
                                ./run.sh firefox latest -Dmaven.test.failure.ignore=true -DforkCount=1 -B
                               """
                        }
                    }
                }
            }
        }
    }
}
parallel branches
