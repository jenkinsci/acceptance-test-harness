# Acceptance tests for Jenkins

End to end test suite for Jenkins automation server and its plugins.

The scenarios are described ain form of tests controlling Jenkins under test (JUT) through UI / REST API. Clean instance
is started for individual tests to isolate the tests. The harness provides convenient docker support so integration tests
can be written easily.

## Getting Started

The simplest way to start the harness is calling `BROWSER=firefox JENKINS_VERSION=latest mvn test`. Complete test suite
takes hours to run due to the number of covered components/use-cases, the cost of Jenkins setup and selenium interactions.
That can be avoided by selecting a subset of tests to be run - smoke tests for instance.

## Further Reading

### Running tests

The harness provides a variety of ways to configure the execution including:

* [Selecting web browser](docs/BROWSER.md)
* [Specifying test(s) to run](docs/SINGLE-TEST.md)
* [Managing the versions of Jenkins and plugins](docs/SUT-VERSIONS.md)
* [Using a http proxy](docs/USING-A-HTTP-PROXY.md)
* [Prelaunching Jenkins](docs/PRELAUNCH.md)
* [Selecting how to launch Jenkins](docs/CONTROLLER.md)
* [Obtaining a report of plugins that were exercised](docs/EXERCISEDPLUGINSREPORTER.md)
* [Running tests in container](docs/DOCKER.md)
* Selecting tests based on plugins they cover (TODO)

### Writing tests

Given how long it takes for the suite to run, test authors are advised to focus on the most popular plugins and
use-cases to maximize the value of the test suite. Tests that can or already are written as a part of core/plugin tests
should be avoided here as well as tests unlikely to catch future regressions (reproducers for individual bugs, boundary
condition testing, etc.). Individual maintainers are expected to update their tests reflecting core/plugin changes as
well as ensuring the tests does not produce false positives. Tests identified to violate this guideline might be removed
without author's notice for the sake of suite reliability.

Areas where acceptance-tests-harness is more suitable then jenkins-test-harness are:

- Installing plugins for cross-plugin integration
- Running tests in realistic classloader environment
- Verifying UI behaviour in actual web browser

* [Docker fixtures](docs/FIXTURES.md)
* [Page objects](docs/PAGE-OBJECTS.md)
    * [Mix-ins](docs/MIXIN.md)
* [Guice is our glue](docs/GUICE.md)
* Writing tests
    * [Video tutorial](https://www.youtube.com/watch?v=ZHAiywgMG-M) by Kohsuke on how to write tests
    * [Writing JUnit test](docs/JUNIT.md)
* [Testing slaves](docs/SLAVE.md)
* [Testing emails](docs/EMAIL.md)
* [Hamcrest matchers](docs/MATCHERS.md)
* [How to use this from your own module](docs/EXTERNAL.md)
* [EC2 provider configuration](docs/EC2-CONFIG.md)
* [Investigation](docs/INVESTIGATION.md)
