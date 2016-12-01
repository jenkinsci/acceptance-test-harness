# Acceptance tests for Jenkins

A project to flesh out some of the [manual test cases for Jenkins
LTS](https://wiki.jenkins-ci.org/display/JENKINS/LTS+RC+Testing) in an automated fashion.

Following areas are still worked on:

 * Vagrant-based `JenkinsController` implementations

master | stable
------ | ------
[![Build Status](https://jenkins.ci.cloudbees.com/buildStatus/icon?job=core/acceptance-test-harness)](https://jenkins.ci.cloudbees.com/job/core/job/acceptance-test-harness/) | [![Build Status](https://jenkins.ci.cloudbees.com/buildStatus/icon?job=core/acceptance-test-harness-stable)](https://jenkins.ci.cloudbees.com/job/core/job/acceptance-test-harness-stable/)

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
* [Using a http proxy](docs/USING-A-HTTP-PROXY.md)
* [Prelaunch JUT](docs/PRELAUNCH.md)
* [Obtaining a report of plugins that were exercised](docs/EXERCISEDPLUGINSREPORTER.md)
* [Managing the versions of Jenkins and plugins](docs/SUT-VERSIONS.md)
* [Investigation](docs/INVESTIGATION.md)
* Selecting tests based on plugins they cover (TODO)

### Writing tests
* [Docker fixtures](docs/FIXTURES.md)
* [Page objects](docs/PAGE-OBJECTS.md)
    * [Mix-ins](docs/MIXIN.md)
* [Guice is our glue](docs/GUICE.md)
* Writing tests
    * [Video tutorial](https://www.youtube.com/watch?v=ZHAiywgMG-M) by Kohsuke on how to write tests
    * [Writing JUnit test](docs/JUNIT.md)
    * Writing Cucumber test (TODO)
* [Testing slaves](docs/SLAVE.md)
* [Testing emails](docs/EMAIL.md)
* explain how cucumber hooks into Guice (TODO)
* [Hamcrest matchers](docs/MATCHERS.md)
* [How to use this from your own module](docs/EXTERNAL.md)
* [EC2 provider configuration](docs/EC2-CONFIG.md)
