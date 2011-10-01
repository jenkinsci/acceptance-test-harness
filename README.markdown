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


## Current test matrix

The tests cases that have been completed or nede to be completed can be found
on the [Selenium Test
Cases](https://wiki.jenkins-ci.org/display/JENKINS/Selenium+Test+Cases) page on
the Jenkins wiki



## What running tests looks like

There is a bit of a delay since we bring up Jenkins for every single test, with
it's own sandboxed workspace as well:

![](http://strongspace.com/rtyler/public/selenium-jenkins.png)


