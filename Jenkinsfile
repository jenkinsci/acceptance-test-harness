// For ci.jenkins.io
// https://github.com/jenkins-infra/documentation/blob/master/ci.adoc

properties([disableConcurrentBuilds(abortPrevious: true)])

if (env.BRANCH_IS_PRIMARY) {
  properties([
          buildDiscarder(logRotator(numToKeepStr: '50')),
          pipelineTriggers([cron('0 18 * * 2')]),
          disableConcurrentBuilds(abortPrevious: true),
  ])
}

def branches = [:]

def axes = [
  jenkinsVersions: ['lts', 'latest'],
  platforms: ['linux'],
  jdks: [11],
  browsers: ['firefox'],
]

def isSubset = env.CHANGE_ID && pullRequest.labels.contains('dependencies')
def subsetIds = [:]

stage('Record builds and sessions') {
  retry(conditions: [kubernetesAgent(handleNonKubernetes: true), nonresumable()], count: 2) {
    node('maven-11') {
      infra.checkoutSCM()
      def athCommit = sh(script: 'git rev-parse HEAD', returnStdout: true).trim()
      withCredentials([string(credentialsId: 'launchable-jenkins-acceptance-test-harness', variable: 'LAUNCHABLE_TOKEN')]) {
        sh 'launchable verify && launchable record commit'
      }
      axes['jenkinsVersions'].each { jenkinsVersion ->
        infra.withArtifactCachingProxy {
          sh "rm -rf target && DISPLAY=:0 ./run.sh firefox ${jenkinsVersion} -Dmaven.repo.local=${WORKSPACE_TMP}/m2repo -B clean process-test-resources"
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
        axes.values().combinations {
          def (jenkinsVersion, platform, jdk, browser) = it
          def sessionFile = "launchable-session-${jenkinsVersion}-${platform}-jdk${jdk}-${browser}.txt"
          sh "launchable record session --build ${env.BUILD_TAG}-${jenkinsVersion} --flavor platform=${platform} --flavor jdk=${jdk} --flavor browser=${browser} >${sessionFile}"
          def session = readFile(sessionFile).trim()
          stash name: sessionFile, includes: sessionFile
          def target = isSubset ? '30%' : '100%'
          subsetIds["${jenkinsVersion}-${platform}-jdk${jdk}-${browser}"] = sh(returnStdout: true, script: "launchable subset --session ${session} --target ${target} --get-tests-from-previous-sessions --split maven").trim()
        }
      }
    }
  }
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

def bins = isSubset ? 1 : 5
for (int i = 1; i <= bins; i++) {
  int index = i
  axes.values().combinations {
    def (jenkinsVersion, platform, jdk, browser) = it
    def name = "${jenkinsVersion}-${platform}-jdk${jdk}-${browser}-split${index}"
    branches[name] = {
      stage(name) {
        retry(count: 2, conditions: [agent(), nonresumable()]) {
          node('docker-highmem') {
            checkout scm
            sh 'mkdir -p target/ath-reports && chmod a+rwx target/ath-reports'
            def cwd = pwd()
            withCredentials([string(credentialsId: 'launchable-jenkins-acceptance-test-harness', variable: 'LAUNCHABLE_TOKEN')]) {
              def subsetId = subsetIds["${jenkinsVersion}-${platform}-jdk${jdk}-${browser}"]
              sh "launchable verify && launchable split-subset --subset-id ${subsetId} --bin ${index}/${bins} --output-exclusion-rules maven >excludes.txt"
            }
            docker.image('jenkins/ath').inside("-v /var/run/docker.sock:/var/run/docker.sock -v '${cwd}/target/ath-reports:/reports:rw' --shm-size 2g") {
              realtimeJUnit(testResults: 'target/surefire-reports/TEST-*.xml', testDataPublishers: [[$class: 'AttachmentPublisher']]) {
                sh """
                    set-java.sh ${jdk}
                    eval \$(vnc.sh)
                    java -version
                    run.sh ${browser} ${jenkinsVersion} -Dmaven.repo.local=${WORKSPACE_TMP}/m2repo -Dmaven.test.failure.ignore=true -DforkCount=1 -B
                    cp --verbose target/surefire-reports/TEST-*.xml /reports
                    """
              }
            }
            withCredentials([string(credentialsId: 'launchable-jenkins-acceptance-test-harness', variable: 'LAUNCHABLE_TOKEN')]) {
              def sessionFile = "launchable-session-${jenkinsVersion}-${platform}-jdk${jdk}-${browser}.txt"
              unstash sessionFile
              def session = readFile(sessionFile).trim()
              sh "launchable verify && launchable record tests --session ${session} maven './target/ath-reports'"
            }
          }
        }
      }
    }
  }
}
parallel branches
infra.maybePublishIncrementals()
