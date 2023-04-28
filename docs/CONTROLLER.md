# Choosing the JenkinsController
This test harness has an abstraction called [JenkinsController](../src/main/java/org/jenkinsci/test/acceptance/controller/JenkinsController.java)
that allows you to use different logic for starting/stopping Jenkins.
We use this so that the same set of tests can be run against many different ways of launching Jenkins,
such as `java -jar jenkins.war`, Jenkins on JBoss, Jenkins via a debian package, etc.

To select a controller, run the test with the `TYPE` environment variable set to the controller ID, such as:
`TYPE=remote_sysv mvn test`.

Common configuration of Controllers can be done through environment variables, and the following controller-specific
section describes them.

For more sophisticated customization, see [WIRING.md](WIRING.md).

## Local family controllers
All local controllers run both test harness and Jenkins under Test on the local machine. Common environment variables for local controllers:

* `JENKINS_JAVA_HOME` the JVM home to use for running Jenkins. If not specified, the first of `JAVA_HOME`, or the JVM
   used to launch the tests will be used.
* `JENKINS_JAVA_OPTS` Adds additional options to the java process like `-Xms=XXm -Xmx=XXXm`.
* `INTERACTIVE` keep browser session opened after failed scenario for interactive investigation.

You can disable the logging output of Jenkins by setting the system property `quiet` on 
the command line.

### Winstone controller (TYPE=winstone)
This controller runs Jenkins via `java -jar jenkins.war` on the same host where the test is run. This is the default controller.

### Winstone Docker controller (TYPE=winstone_docker)
This controller runs Jenkins via `java -jar jenkins.war` much like the Winstone controller, except
that it launches Winstone inside a Docker container. This allows users to better control the environment
in which Jenkins runs in, without incurring the overhead of Vagrant.

Variables:

* `DOCKER_IMAGE` the docker container image name (or image ID) in which to run Jenkins.

The ability to specify the Docker image is particularly useful to ensure that tests are run against
a consistent version of JDK and build tools.

### Tomcat controller (TYPE=tomcat)
This controller deploys Jenkins inside Tomcat and runs the test with it.
This controller requires a functioning Tomcat installation listening on port 8080, on the same system that the tests run.
During the test, Jenkins is deployed here, and Tomcat gets started/stopped.

The behaviour of this controller can be customized through the following environment variables:

* `CATALINA_HOME` The directory in which Tomcat is already installed. If this is not specified, the first existing
    directory from the following list will be used: a `tomcat` directory as a sibling to the resolved `jenkins.war`,
    `WORKSPACE/tomcat` and `$(pwd)/tomcat`.

### JBoss controller (TYPE=jboss)
Similar to the above Tomcat controller except it uses JBoss.

The behaviour of this controller can be customized through the following environment variables:

* `JBOSS_HOME` The directory in which JBoss is already installed.  If this is not specified, the first existing
    directory from the following list will be used: a `jboss` directory as a sibling to the resolved `jenkins.war`,
    `WORKSPACE/jboss` and `$(pwd)/jboss`.

## 'Existing Jenkins' controller (TYPE=existing)
This controller assumes that you already have a Jenkins instance somewhere that you want the tests to run in.
Note that tests mutate this Jenkins instance, so this is not meant to be used with your production Jenkins instance.
Instead, run `java -jar jenkins.war` somewhere, and use this controller.

The behaviour of this controller can be customized through the following environment variables:

* `JENKINS_URL` the URL to the running Jenkins. Defaults to `http://localhost:8080/`

This controller is useful when you want to debug Jenkins while you run a test. It can be also used during
iterative test development to execute tests quickly.
