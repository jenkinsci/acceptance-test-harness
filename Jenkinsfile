// For ci.jenkins.io
// https://github.com/jenkins-infra/documentation/blob/master/ci.adoc

properties([
  disableConcurrentBuilds(abortPrevious: true),
  buildDiscarder(logRotator(numToKeepStr: '5')),
])

if (env.BRANCH_IS_PRIMARY) {
  properties([
          buildDiscarder(logRotator(numToKeepStr: '10')),
          pipelineTriggers([cron('0 18 * * 2')]),
          disableConcurrentBuilds(abortPrevious: true),
  ])
}

// TEMPORARY: runs a single test 10 times to measure flakiness. Do not merge.
def test = 'JobDslPluginTest#should_run_grooy_sandbox_as_particular_user'
def branches = [:]
for (int i = 0; i < 10; i++) {
  int index = i
  branches["repeat${index}"] = {
    stage("repeat${index}") {
      node('docker-highmem && nonspot') {
        checkout scm
        sh './build-image.sh'
        infra.withArtifactCachingProxy {
          withEnv(["MAVEN_ARGS=${env.MAVEN_ARGS ?: ''} -Dtest=${test}"]) {
            realtimeJUnit(testResults: 'target/surefire-reports/TEST-*.xml',
                          testDataPublishers: [[$class: 'AttachmentPublisher']]) {
              sh './ci.sh 21 firefox latest'
            }
          }
        }
      }
    }
  }
}
parallel branches
