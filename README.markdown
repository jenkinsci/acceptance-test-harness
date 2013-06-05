# Selenium tests for Jenkins

This is a project to flesh out some of the [manual test cases for Jenkins
LTS](https://wiki.jenkins-ci.org/display/JENKINS/LTS+RC+Testing) in
an automated fashion. Up to date test results can be found at https://jenkins.ci.cloudbees.com/job/selenium-tests.

Right now the project is in a very early state, and is in dire need of some
[Page Objects](https://code.google.com/p/selenium/wiki/PageObjects) for the
more standard components of Jenkins such as the:

 * Root actions link listing (top left sidebar)
 * New Job control
 * Various plugin configuration sections on the `job/configure` page
 * Node configuration
 * etc

Drop me a line (`rtyler` on [Freenode](http://freenode.net)) if you're
interested in helping out

## Installing

On Ubuntu you may need to install: libxslt1-dev, libxml2-dev, libcurl4-openssl-dev

## Current test matrix

The tests cases that have been completed or nede to be completed can be found
on the [Selenium Test
Cases](https://wiki.jenkins-ci.org/display/JENKINS/Selenium+Test+Cases) page on
the Jenkins wiki

For historical reasons, there are older tests that are written for `test/unit` (in the `test` directory)
and newer tests that are written for cucumber (in the `features` directory.)

## Running tests

First, run `bundle install` to install required dependencies.

To run the test, `JENKINS_WAR=path/to/your/jenkins.war bundle exec rake`.
This runs the entire test suite, so it might take a while.

Set `BROWSER=chrome` and install http://code.google.com/p/chromedriver/downloads/list in `$PATH` if desired. (But tests may fail.)

There is a bit of a delay since we bring up Jenkins for every single test, with
it's own sandboxed workspace as well:

![](http://strongspace.com/rtyler/public/selenium-jenkins.png)


### Choosing the JenkinsController
This test harness has an abstraction called `JenkinsController` that allows you to use different logic
for starting/stopping Jenkins. We use this to so that the same set of tests can be run against many different ways of launching Jenkins, such as `java -jar jenkins.war`, Jenkins on JBoss, Jenkins via debian package, etc.

See [the source code](tree/master/lib/controller/) for the list of available controllers. If you see a line like
`register :remote_sysv`, that means the ID of that controller is `remote_sysv`.

To select a controller, run the test with the 'type' environment variable set to the controller ID, such as:
`type=remote_sysv bundle exec rake`. Controllers take their configurations from environment variables. Again,

#### Winstone controller (type=winstone)
This controller runs Jenkins via `java -jar jenkins.war` on the same host where the test is run.
The behaviour of this controller can be customized through the following environment variables.

* `JENKINS_WAR` the path to `jenkins.war` to be tested.
* `INTERACTIVE` keep browser session opened after failed scenario for interactive investigation.
* `PLUGINS_DIR` a directory of plugins to be loaded on Jenkins startup

#### Tomcat controller (type=tomcat)
This controller deploys Jenkins inside Tomcat and run the test with it. This controller requires a functioning Tomcat installation listening on port 8080, on the same system that the tests run. During the test, Jenkins is deployed here, and Tomcat gets started/stopped.

The behaviour of this controller can be customized through the following environment variables.

* `JENKINS_WAR` see above
* `INTERACTIVE` see above
* `PLUGINS_DIR` see above
* `CATALINA_HOME` The directory in which Tomcat is already installed.

#### JBoss controller (type=jboss)
Similar to the above Tomcat controller except it uses JBoss.

The behaviour of this controller can be customized through the following environment variables.

* `JENKINS_WAR` see above
* `INTERACTIVE` see above
* `PLUGINS_DIR` see above
* `JBOSS_HOME` The directory in which JBoss is already installed.

#### Ubuntu controller (type=ubuntu)
This controller uses Vagrant to run Ubuntu, then deploy Jenkins from an APT repository as a debian package. (This controller is not yet capable of testing individual `*.deb` file.)

* `REPO_URL` The location of APT repository in the format of `/etc/apt/sources.list`, such as `http://pkg.jenkins-ci.org/debian binary/`

#### CentOS controller (type=centos)
This controller uses Vagrant to run CentOS, then deploy Jenkins from an RPM repository.
This controller is not yet capable of testing individual `*.rpm` file.

* `REPO_URL` The location of RPM repository, such as `http://pkg.jenkins-ci.org/opensuse/`

#### OpenSUSE controller (type=opensuse)
This controller uses Vagrant to run CentOS, then deploy Jenkins from an RPM repository.
This controller is not yet capable of testing individual `*.rpm` file.

* `REPO_URL` The location of RPM repository, such as `http://pkg.jenkins-ci.org/opensuse/`

#### Common characteristics of all the Vagrant-based controllers
When run for the first time, this test harness will create a virtual machine.
To make repeated tests fast, the VM won't get shut down automatically at the end of a run.
To do so, cd `vagrant/*` and run `vagrant halt`. You can run any other vagrant commands
in this manner, which is useful for debugging.

You can also create `pre-configure.sh` and/or `post-configure.sh` in the current directory as needed
to customize how the Vagrant VM is initialized. These scripts are copied into the VM and then executed:

* `pre-configure.sh` runs before the controller attempts to install Jenkins
* `post-configure.sh` runs after the controller finished installing Jenkins



### Running one test
You can run a single cucumber test by pointing to a test scenario in terms of its file name and line number like
`bundle exec cucumber features/freestyle_build.feature:6` or `bundle exec rake FEATURE=features/freestyle_build.feature:6`. Line number can be omitted.

## Describing features

New feature definitions can take advantage of existing step definitions as well
as introduce it's own. It is desirable to reuse steps that already exists and
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
