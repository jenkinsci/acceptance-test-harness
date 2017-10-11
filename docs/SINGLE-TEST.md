# Specifying tests to run

There are several profile that might be handy for specifying tests to run:

- `-PrunSmokeTests` - only the essential tests to execute the most fundamental use cases.
- `-PrunDockerTests` - only tests that require docker.
- `-PskipCucumberTests` - skip tests implementing in Cucumber.
- `-PtestOnlyPlugins` - only tests that require plugins specified in `TEST_ONLY_PLUGINS` environment variable (comma separated artifact ids).

## JUnit
To run a single JUnit test from command line, specify the name of the test with the `-Dtest=` option:

    mvn -Dtest=AntPluginTest#autoInstallAnt test

It need not specify a fully qualified class name.
See [Maven surefire plugin](http://maven.apache.org/surefire/maven-surefire-plugin/examples/single-test.html) for
more details about how to specify a group of tests.

## Cucumber
To run a single Cucumber test from command line, specify a (relative) path to a feature file, optionally
with a line number to specify a specific scenario

    mvn -Dcucumber.test=features/freestyle_build.feature:6 test

