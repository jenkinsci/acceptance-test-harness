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

## Getting Started

Let's start by running tests locally with `JENKINS_WAR=path/to/your/jenkins.war mvn test`.
This runs the entire test suite with the specified Jenkins war and Firefox, so it will take a while.

Set `BROWSER=chrome` and install http://code.google.com/p/chromedriver/downloads/list in `$PATH` if desired. (But tests may fail.)

There is a bit of a delay since we bring up Jenkins for every single test, with
it's own sandboxed workspace.

## Further Reading

### Running tests

* [Selecting browser](blob/master/docs/BROWSER.md)
* [Selecting how to launch Jenkins under test](blob/master/docs/CONTROLLER.md)
* [Running one test](blob/master/docs/SINGLE-TEST.md)

### Writing tests
* [Docker fixtures](blob/amster/docs/FIXTURES.md)
* [Page objects](blob/amster/docs/PAGE-OBJECTS.md)
* how to add page objects?
  * convention of exception handling
* explain two ways: cucumber and junit
 * junit: rule and annotations
* explain guice world that's common to both
* explain how cucumber hooks into Guice
* using scaffold.config for win
* hamcrest matchers
* how to use this from your own module
