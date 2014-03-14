# Running one test
Generally speaking, running one test is the easiest from your IDE. Select one test method of JUnit
or one test scenario of Cucumber, then ask the IDE to run it.

## JUnit
To run a single JUnit test from command line, specify the name of the test with the `-Dtest=` option:

    mvn -Pno-cucumber -Dtest=AntPluginTest#autoInstallAnt test

It need not specify a fully qualified class name.
See [Maven surefire plugin](http://maven.apache.org/surefire/maven-surefire-plugin/examples/single-test.html) for
more details about how to specify a group of tests.

## Cucumber
To run a single Cucumber test from command line, specify a (relative) path to a feature file, optionally
with a line number to specify a specific scenario

    mvn -Pno-junit -Dtest=features/freestyle_build.feature:6 test

TODO: use a driver directly to skip all Maven stuff