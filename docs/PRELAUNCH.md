# Prelaunching Jenkins under test

If you follow [Getting Started section](../README.md), you'll notice that the test executes really slowly.
This is because each test wants to launch its own clean Jenkins, and we end up mostly just waiting for Jenkins
under test (JUT) to come down.

This delay is also quite annoying when you are developing a new test. Often you have to run the test under development
multiple times before you get your test right. And every time you run a test, you end up waiting for JUT to come up.

To help cope with this situation, this project comes with a separate entry point that runs a JUT server.
JUT server will maintain a fixed number of Jenkins instances booted. There's a corresponding `PooledJenkinsController`
implementation you'd use when you run a test, which asks the JUT server to hand off a fresh JUT.

This drastically cuts down the wait time until the actual meat of your test starts.

There's also a related [TYPE=existing JenkinsController](CONTROLLER.md).

## Launching JUT server

To launch a JUT server, run the following command:

    mvn -Pjut-server

The server will keep running until you kill the Maven process. The server listens to
Unix domain socket at `~/jenkins.sock`

JUT server internally uses to other real `JenkinsController` implementations to launch JUT,
and you configure it the same way you configure normal test executions.
See [this document](CONTROLLER.md) for how to select JenkinsController.

To specify the size of the pool, add the `-Dcount=2` or something like that as a Maven option.


## Selecting PooledJenkinsController

If no controller is explicitly specified, the harness checks the presence of `~/jenkins.sock` and
it automatically selects `PooledJenkinsController`.

To select this controller explicitly, use `TYPE=pool` environment variable.