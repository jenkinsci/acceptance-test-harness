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

def branches = [:]
def splits
def needSplittingFromWorkspace = true

// TODO could use launchable split-subset to split this into bins

for (build = currentBuild.previousCompletedBuild; build != null; build = build.previousCompletedBuild) {
  if (build.resultIsBetterOrEqualTo('UNSTABLE')) {
    // we have a reference build
    echo "not splitting from workspace, reference build should be : ${build.projectName}:${build.number}, with state ${build.result}"
    needSplittingFromWorkspace = false
    break
  }
}

if (needSplittingFromWorkspace) {
  node { // When there are no previous build, we need to estimate splits from files which require workspace
    checkout scm
    splits = splitTests estimateTestsFromFiles: true, parallelism: count(10)
  }
} else {
  splits = splitTests count(10)
}

// Default jdk(s) tested for each Jenkins version line.
def jenkinsVersionJdks = [
  lts: [21],
  latest: [21, 25],
]

// Labels on the PR (weekly-test, lts-test, java-$version), or marker files of the same name at the
// repository root for contributors without permission to add labels, allow overriding what gets
// tested.
def weeklyTestMarkerFile
def ltsTestMarkerFile
def javaVersionMarkerFiles
def jenkinsVersions
def jdksFor

def axes = [
  platforms: ['linux'],
  browsers: ['firefox'],
]

stage('Record builds and sessions') {
  retry(conditions: [kubernetesAgent(handleNonKubernetes: true), nonresumable()], count: 2) {
    node('maven-25') {
      infra.checkoutSCM()

      // pullRequest.labels returns a one-shot Stream, materialize it so it can be queried more than once.
      def labels = (env.CHANGE_ID) ? pullRequest.labels.toList() : []
      weeklyTestMarkerFile = fileExists 'weekly-test'
      ltsTestMarkerFile = fileExists 'lts-test'
      javaVersionMarkerFiles = sh(script: 'ls -1 java-* 2>/dev/null || true', returnStdout: true).trim().tokenize('\n').findAll { it ==~ /java-\d+/ }

      def weeklyTest = weeklyTestMarkerFile || labels.contains('weekly-test')
      def ltsTest = ltsTestMarkerFile || labels.contains('lts-test')
      def javaVersions = (
        javaVersionMarkerFiles + labels.findAll { it ==~ /java-\d+/ }
      ).collect { (it - 'java-') as Integer }.unique()

      jenkinsVersions = []
      if (weeklyTest) {
        jenkinsVersions << 'latest'
      }
      if (ltsTest) {
        jenkinsVersions << 'lts'
      }
      if (jenkinsVersions.isEmpty()) {
        // No override label/marker file present, test the default set of Jenkins version lines.
        jenkinsVersions = jenkinsVersionJdks.keySet() as List
      }

      // jdk(s) to test a given Jenkins version line with, honoring the java-$version label(s)/marker file(s) if present.
      jdksFor = { String jenkinsVersion ->
        javaVersions ?: jenkinsVersionJdks[jenkinsVersion]
      }

      def athCommit = sh(script: 'git rev-parse HEAD', returnStdout: true).trim()
      withCredentials([string(credentialsId: 'launchable-jenkins-acceptance-test-harness', variable: 'LAUNCHABLE_TOKEN')]) {
        sh 'launchable verify && launchable record commit'
      }
      jenkinsVersions.each { jenkinsVersion ->
        infra.withArtifactCachingProxy {
            sh "rm -rf target && DISPLAY=:0 ./src/main/resources/ath-container/run.sh firefox ${jenkinsVersion} -Dmaven.repo.local=${WORKSPACE_TMP}/m2repo -B clean process-test-resources"
        }
        def coreCommit = sh(script: './core-commit.sh', returnStdout: true).trim()
        /*
         * TODO Add the commits of the transitive closure of the Jenkins WAR under test and the ATH
         * JAR to this build.
         */
        withCredentials([string(credentialsId: 'launchable-jenkins-acceptance-test-harness', variable: 'LAUNCHABLE_TOKEN')]) {
          sh "launchable verify && launchable record build --name ${env.BUILD_TAG}-${jenkinsVersion} --no-commit-collection --commit jenkinsci/acceptance-test-harness=${athCommit} --commit jenkinsci/jenkins=${coreCommit}"
        }
      }
      withCredentials([string(credentialsId: 'launchable-jenkins-acceptance-test-harness', variable: 'LAUNCHABLE_TOKEN')]) {
        jenkinsVersions.each { jenkinsVersion ->
          axes.platforms.each { platform ->
            jdksFor(jenkinsVersion).each { jdk ->
              axes.browsers.each { browser ->
                def sessionFile = "launchable-session-${jenkinsVersion}-${platform}-jdk${jdk}-${browser}.txt"
                sh "launchable record session --build ${env.BUILD_TAG}-${jenkinsVersion} --flavor platform=${platform} --flavor jdk=${jdk} --flavor browser=${browser} >${sessionFile}"
                stash name: sessionFile, includes: sessionFile
              }
            }
          }
        }
      }
    }
  }
}

branches['CI'] = {
  stage('CI') {
    retry(count: 2, conditions: [kubernetesAgent(handleNonKubernetes: true), nonresumable()]) {
      node('maven-25') {
        checkout scm
        def mavenOptions = [
          '-Dset.changelist',
          '-DskipTests',
          '-U',
          'clean',
          'install',
        ]
        infra.runMaven(mavenOptions, 25)
        infra.prepareToPublishIncrementals()
      }
    }
  }
}

for (int i = 0; i < splits.size(); i++) {
  int index = i
  jenkinsVersions.each { jenkinsVersion ->
    axes.platforms.each { platform ->
      jdksFor(jenkinsVersion).each { jdk ->
        axes.browsers.each { browser ->
          def name = "${jenkinsVersion}-${platform}-jdk${jdk}-${browser}-split${index}"
          branches[name] = {
            stage(name) {
              int retryCounts = 1
              retry(count: 2, conditions: [agent(), nonresumable()]) {
                String nodeLabel = 'docker-highmem && nonspot'
                if (retryCounts == 1) {
                  // Use a spot instance for the first try
                  nodeLabel = 'docker-highmem && spot'
                }
                retryCounts = retryCounts + 1 // increment the retry count before allocating a node in case it fails
                node(nodeLabel) {
                  checkout scm
                    sh './build-image.sh'
                    def exclusions = splits.get(index).join('\n')
                    writeFile file: 'excludes.txt', text: exclusions
                    infra.withArtifactCachingProxy {
                      realtimeJUnit(
                          testResults: 'target/surefire-reports/TEST-*.xml',
                          testDataPublishers: [[$class: 'AttachmentPublisher']],
                          // Slow test(s) removal can causes a split to get empty which otherwise fails the build.
                          // The build failure prevents parallel tests executor to realize the tests are gone so same
                          // split is run to execute and report zero tests - which fails the build. Permit the test
                          // results to be empty to break the circle: build after removal executes one empty split
                          // but not letting the build to fail will cause next build not to try those tests again.
                          allowEmptyResults: true
                          ) {
                            sh "echo ./ci.sh ${jdk} ${browser} ${jenkinsVersion}"
                          }
                  }
//                   withCredentials([string(credentialsId: 'launchable-jenkins-acceptance-test-harness', variable: 'LAUNCHABLE_TOKEN')]) {
//                     def sessionFile = "launchable-session-${jenkinsVersion}-${platform}-jdk${jdk}-${browser}.txt"
//                     unstash sessionFile
//                     def session = readFile(sessionFile).trim()
//                     sh "launchable verify && launchable record tests --session ${session} maven './target/surefire-reports'"
//                   }
                }
              }
            }
          }
        }
      }
    }
  }
}
parallel branches

stage('checks') {
  if (weeklyTestMarkerFile) {
    unstable 'Remember to `git rm weekly-test` before merging'
  }
  if (ltsTestMarkerFile) {
    unstable 'Remember to `git rm lts-test` before merging'
  }
  if (javaVersionMarkerFiles) {
    unstable "Remember to `git rm ${javaVersionMarkerFiles.join(' ')}` before merging"
  }
}

infra.maybePublishIncrementals()
