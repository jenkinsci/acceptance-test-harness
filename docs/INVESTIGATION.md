# Investigating failed test

## Interactive investigation

As an alternative to set a breakpoint, that would stop the test suite and keep Jenkins and browser running for manual investigation. There is a environment variable `INTERACTIVE=true` that, when provided, will pause the suite whenever test fails/throws exception.

You can also add a call to `InteractiveConsole.execute(this)` in the code to stop the execution and debug in interactive groovy console.

## Diagnostic information

Test harness keeps track of test diagnostic information in `/target/diagnostics/<TESTNAME>` directory. For every reported file there is a [JUnit Attachments](https://wiki.jenkins-ci.org/display/JENKINS/JUnit+Attachments+Plugin) marker line printed in order to attach the diagnostic information to the test result when run in Jenkins.
