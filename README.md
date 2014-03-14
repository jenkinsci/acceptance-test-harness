# Acceptance tests for Jenkins

This is an attempted Java-ported version of [selenium-tests](https://github.com/jenkinsci/selenium-tests),
which is a project to flesh out some of the [manual test cases for Jenkins
LTS](https://wiki.jenkins-ci.org/display/JENKINS/LTS+RC+Testing) in
an automated fashion.

Right now the project is in a very early state. The following parts are working:

 * Drive Cucumber to run tests
 * Many key page object types are ported, even though they are still missing many methods
 * Some step definitions are ported, but more needs to be poreted

Following areas are still worked on:

 * `JenkinsController`
 * Docker support

[![Build Status](https://jenkins.ci.cloudbees.com/buildStatus/icon?job=core/acceptance-test-harness)](https://jenkins.ci.cloudbees.com/job/core/job/acceptance-test-harness/)

## Running tests

Let's start by running tests locally with `JENKINS_WAR=path/to/your/jenkins.war mvn test`.
This runs the entire test suite with the specified Jenkins war and Firefox, so it might take a while.

Set `BROWSER=chrome` and install http://code.google.com/p/chromedriver/downloads/list in `$PATH` if desired. (But tests may fail.)

There is a bit of a delay since we bring up Jenkins for every single test, with
it's own sandboxed workspace.

### Configuring how tests are run
There are several ways to control how Jenkins under test gets launched and what browser is used for testing.

The simpler way to do it is through environment variables. This approach lets you customize
two important dimensions of configuration:

* `BROWSER`: browser to be used to run the tests. Default `firefox`.
* `TYPE`: how to launch Jenkins under test. Default to `winstone`.

In addition to those, there are several environment variables that affect behaviours of test harness locally:

* `STARTUP_TIMEOUT` Timeout in seconds for Jenkins to start. Default `100`.

See `CONTROLLER.md` for controlling how to launch Jenkins under test.

### Running one test
You can run a single cucumber test by pointing to a test scenario in terms of its file name and line number like
`bundle exec cucumber features/freestyle_build.feature:6` or `bundle exec rake FEATURE=features/freestyle_build.feature:6`. Line number can be omitted.

## Describing features

New feature definitions can take advantage of existing step definitions as well
as introduce its own. It is desirable to reuse steps that already exists and
keep feature specific steps separated at the same time.

While creating new features you can check whether all steps are declared
and unambiguous using `bundle exec rake cucumber:dryrun` as it is
considerably faster that actually running the scenarios.

### Features

All features are located in `features` directory and the filenames are suffixed
with `.feature` or `_plugin.feature` provided the feature describes functionality
of a plugin.

```
features/configure_slaves.feature
features/git_plugin.feature
```

### Step definitions

All step definitions are located in `features/step_definitions` directory or
`features/step_definitions/plugins` provided the feature describes functionality
of a plugin. Filenames are suffixed with `_steps.rb`.

```
features/step_definitions/job_steps.rb
features/step_definitions/plugins/ant_steps.rb
```
### Page objects

All page object resides in `lib` directory or `lib/plugins/`.

```
lib/build.rb
lib/plugins/git.rb
```

Special kind of page object, PageArea, can be used to represent several controls
within a page object (one buildstep for instance).

### Resources

Features might need specific resources as a fixture. Directory `resources` is
dedicated for that purpose.

```
resources/cobertura_plugin/
```
