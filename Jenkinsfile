// For ci.jenkins.io
// https://github.com/jenkins-infra/documentation/blob/master/ci.adoc

properties([disableConcurrentBuilds(abortPrevious: true)])

if (env.BRANCH_IS_PRIMARY) {
  properties([
          buildDiscarder(logRotator(numToKeepStr: '50')),
          pipelineTriggers([cron('0 18 * * 2')]),
  ])
}

def branches = [:]
def splits
def needSplittingFromWorkspace = true

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

branches['CI'] = {
  stage('CI') {
    retry(count: 2, conditions: [kubernetesAgent(handleNonKubernetes: true), nonresumable()]) {
      node('maven-11') {
        checkout scm
        def mavenOptions = [
          '-Dset.changelist',
          '-DskipTests',
          '-U',
          'clean',
          'install',
        ]
        infra.runMaven(mavenOptions, 11)
        infra.prepareToPublishIncrementals()
      }
    }
  }
}

for (int i = 0; i < splits.size(); i++) {
  int index = i
  for (int j in [11]) {
    for (String v in ['lts', 'latest']) {
      int javaVersion = j
      String jenkinsUnderTest = v
      def name = "java-${javaVersion}-jenkins-${jenkinsUnderTest}-split${index}"
      branches[name] = {
        stage(name) {
         retry(count: 2, conditions: [agent(), nonresumable()]) {
          node('docker-highmem') {
            checkout scm
            def browser = 'firefox'
            def image = docker.build('jenkins/ath', '--build-arg uid="$(id -u)" --build-arg gid="$(id -g)" ./src/main/resources/ath-container/')
            sh 'mkdir -p target/ath-reports && chmod a+rwx target/ath-reports'
            def cwd = pwd()
            image.inside("-v /var/run/docker.sock:/var/run/docker.sock -v '${cwd}/target/ath-reports:/reports:rw' --shm-size 2g") {
              def exclusions = splits.get(index).join('\n')
              writeFile file: 'excludes.txt', text: exclusions
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
                sh """
                    set-java.sh $javaVersion
                    eval \$(vnc.sh)
                    java -version
                    run.sh ${browser} ${jenkinsUnderTest} -Dmaven.repo.local=${WORKSPACE_TMP}/m2repo -Dmaven.test.failure.ignore=true -DforkCount=1 -B
                    cp --verbose target/surefire-reports/TEST-*.xml /reports
                """
              }
            }
            launchable.install()
            withCredentials([string(credentialsId: 'launchable-jenkins-acceptance-test-harness', variable: 'LAUNCHABLE_TOKEN')]) {
              launchable('verify')
              /*
               * TODO Create a Launchable build and session earlier, and replace "--no-build" with
               * "--session" to associate these test results with a particular build. The commits
               * associated with the Launchable build should be the commits of the transitive closure of
               * the Jenkins WAR under test in this build as well as the commits of the transitive closure
               * of the ATH JAR.
               */
              launchable("record tests --no-build --flavor platform=linux --flavor jdk=${javaVersion} --flavor browser=${browser} maven './target/ath-reports'")
            }
          }
         }
        }
      }
    }
  }
}
parallel branches
infra.maybePublishIncrementals()
