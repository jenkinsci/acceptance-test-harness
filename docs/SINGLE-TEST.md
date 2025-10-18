# Specifying tests to run

There are several profiles that might be handy for specifying tests to run:

- `-PrunSmokeTests` - only the essential tests to execute the most fundamental use cases.
- `-PrunDockerTests` - only tests that require docker.
- `-PskipCucumberTests` - skip tests implemented in Cucumber.

## JUnit
To run a single JUnit test from the command line, specify the name of the test with the `-Dtest=` option:

    mvn -Dtest=AntPluginTest#autoInstallAnt test

It need not specify a fully qualified class name.
See [Maven surefire plugin](http://maven.apache.org/surefire/maven-surefire-plugin/examples/single-test.html) for
more details about how to specify a group of tests.
