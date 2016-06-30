/*
 * This Jenkinsfile is intended to run on https://jenkins.ci.cloudbees.com and may fail anywhere else.
 * It makes assumptions about plugins being installed, labels mapping to nodes that can build what is needed, etc.
 *
 * The required label is "hi-speed" */

// Only keep the 50 most recent builds.
properties([[$class: 'jenkins.model.BuildDiscarderProperty', strategy: [$class: 'LogRotator',
                                                                        numToKeepStr: '50']]])

loadParameterizedLibs { functions, ath ->

    parallel 'jenkins-war': { functions.stashJenkinsWar(war, athLabel(), true) },
        'get-ath': { stashAth(functions) },
        failFast: true


}

def loadParameterizedLibs(def body) {
    String functionsRepo = FUNCTIONS_REPO
    String functionsBranch = FUNCTIONS_BRANCH
    String label = FUNCTIONS_LABEL
    loadLibs(label, functionsRepo, functionsBranch, body)
}

def loadLibs(String label = null,
             String functionsRepo = "jenkinsci/jenkins-project-pipeline-lib",
             String functionsBranch = "master",
             def body) {
    if (label == null) {
        label = athLabel()
    }

    if (!functionsRepo.endsWith(".git")) {
        functionsRepo = functionsRepo + ".git"
    }
    
    def functions
    def ath

    node(label) {
        git changelog: false, poll: false, url: "git://github.com/" + functionsRepo, branch: functionsBranch

        functions = load 'lib/functions.groovy'
        ath = load 'lib/ath.groovy'
    }

    body(functions, ath)
}
