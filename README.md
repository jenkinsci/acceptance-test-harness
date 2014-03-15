# Acceptance tests for Jenkins

This is an attempted Java-ported version of [selenium-tests](https://github.com/jenkinsci/selenium-tests),
which is a project to flesh out some of the [manual test cases for Jenkins
LTS](https://wiki.jenkins-ci.org/display/JENKINS/LTS+RC+Testing) in
an automated fashion. The intent is to retire the selenium-tests module when this project achieves
parity.

### Porting status
Right now the following parts are working:

 * Drive Cucumber to run tests
 * Many key page object types are ported, even though they are still missing many methods
 * Some step definitions are ported, but more needs to be poreted
 * Docker support

Following areas are still worked on:

 * Vagrant-based `JenkinsController` implementations
 * Either more steps need to be written to make existing cucumber tests work,
   or they need to be ported over to JUnit.

[![Build Status](https://jenkins.ci.cloudbees.com/buildStatus/icon?job=core/acceptance-test-harness)](https://jenkins.ci.cloudbees.com/job/core/job/acceptance-test-harness/)

## Getting Started

Let's start by running tests locally with `JENKINS_WAR=path/to/your/jenkins.war mvn test`.
This runs the entire test suite with the specified Jenkins war and Firefox, so it will take a while.

Set `BROWSER=chrome` and install http://code.google.com/p/chromedriver/downloads/list in `$PATH` if desired. (But tests may fail.)

There is a bit of a delay since we bring up Jenkins for every single test, with
it's own sandboxed workspace.

## Further Reading

### Running tests

* [Selecting browser](docs/BROWSER.md)
* [Selecting how to launch Jenkins under test (JUT)](docs/CONTROLLER.md)
* [Running one test](docs/SINGLE-TEST.md)
* [Prelaunch JUT](docs/PRELAUNCH.md)

### Writing tests
* [Docker fixtures](docs/FIXTURES.md)
* [Page objects](docs/PAGE-OBJECTS.md)
* [Guice is our glue](docs/GUICE.md)
* explain two ways: cucumber and junit
 * junit: rule and annotations
* explain guice world that's common to both
* explain how cucumber hooks into Guice
* hamcrest matchers
* how to use this from your own module
