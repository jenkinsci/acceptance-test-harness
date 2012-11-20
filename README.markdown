# Selenium tests for Jenkins

This is a project to flesh out some of the [manual test cases for
Jenkins LTS](https://wiki.jenkins-ci.org/display/JENKINS/LTS+1.409.x+RC+Testing) in an automated fashion.

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
see the controller source code for details until we document them here.

### Running one test
You can run a single cucumber test by pointing to a test scenario in terms of its file name and line number like
`bundle exec cucumber features/freestyle_build.feature:6`
