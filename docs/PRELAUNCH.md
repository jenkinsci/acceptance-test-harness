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

    JENKINS_WAR=/path/to/jenkins.war ./jut-server.sh

The server will keep running until you kill the Maven process. The server listens to
Unix domain socket at `~/jenkins.sock`. You can change the name of the socket using 
the environment variable `JUT_SOCKET`:

    JENKINS_WAR=/path/to/jenkins.war JUT_SOCKET=/path/to/jenkins.sock ./jut-server.sh

JUT server internally uses to other real `JenkinsController` implementations to launch JUT,
and you configure it the same way you configure normal test executions. That is, the above example
actually uses `WinstoneController` (the default controller type), which recognizes `JENKINS_WAR` environment
variable. See [this document](CONTROLLER.md) for how to select JenkinsController properly.

To specify the size of the pool explicitly, add the option `-n N` where N defines the number of instances, 
e.g. in order to start 2 instances, run: 

    JENKINS_WAR=/path/to/jenkins.war ./jut-server.sh -n 2

## Selecting PooledJenkinsController

If no controller is explicitly specified, the harness checks the presence of `~/jenkins.sock` and
it automatically selects `PooledJenkinsController`. If you did change the name of the socket (see section
[Launching JUT server](#launching-jut-server)) then you also need to specify the name using the 
`JUT_SOCKET` environment variable when running the tests.

To select this controller explicitly, use `TYPE=pool` environment variable.
