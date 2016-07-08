/*
 * This Jenkinsfile is intended to run on https://jenkins.ci.cloudbees.com and may fail anywhere else.
 * It makes assumptions about plugins being installed, labels mapping to nodes that can build what is needed, etc.
 *
 * The required label is "hi-speed" */

// TODO: Figure out how to run just changed/affected-by-changes tests when running from a PR.
// TODO: Get https://issues.jenkins-ci.org/browse/JENKINS-33274 fixed and configured so that we don't have to run
//   serially for the first run on a branch.

timestamps {
    // Only keep the 50 most recent builds.
    properties([[$class: 'jenkins.model.BuildDiscarderProperty', strategy: [$class: 'LogRotator',
                                                                            numToKeepStr: '50']]])

    stage "Load libs"
    loadLibs { functions, ath ->

        stage "Fetch WAR and ATH source"
        parallel 'jenkins-war': { functions.stashJenkinsWar(war, ath.athLabel(), true) },
            'get-ath': { ath.stashAth(functions) },
            failFast: true

        stage "Run tests"
        def branches = ath.getAthBranches(
            functions,       // Used for things like getting the Maven environment set up.
            ath.athLabel(),  // What label to use for execution.
            false,           // Whether to archive JUnit report files for processing in a later job.
            3,               // Number of times to retry failed tests
            4                // Parallel branch count, in addition to the cucumber branch
        )

        parallel branches
    }
}

/**
 * Loads the libraries using known parameters for repo/branch/node if they're specified, and then makes the libraries
 *   available in the execution of the body.
 *
 * @param body Closure to execute.
 */
def loadLibs(def body) {
    String functionsRepo = defaultValueIfNotSet("FUNCTIONS_REPO", "jenkinsci/jenkins-project-pipeline-lib")
    String functionsBranch = defaultValueIfNotSet("FUNCTIONS_BRANCH", "master")
    String label = defaultValueIfNotSet("FUNCTIONS_LABEL", "hi-speed")

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

/**
 * Given a parameter name and an optional default value, try to get the value of that parameter. If the parameter
 *   exists, it's not null, and its value isn't the empty string, return that. Otherwise, return the default value.
 *
 * @param paramName A String name of a build parameter
 * @param defaultValue An optional default value if the parameter does not exist, is unset or is the empty string.
 * @return The value of the parameter if appropriate, the default value otherwise.
 */
def defaultValueIfNotSet(String paramName, def defaultValue = null) {
    def result = defaultValue

    try {
        def prop = getProperty(paramName)
        if (prop != null && prop != '') {
            result = prop
        }
    } catch (Exception e) {
        // This just means the parameter doesn't exist at all, so let's move on.
    }

    return result
}