# Testing Slaves
Just like [JenkinsController](CONTROLLER.md) allows test runners to test Jenkins with the environment of their choice,
this test harness allows test runners to control how slaves are set up to run tests.

For this purpose, we define `SlaveProvider` and `SlaveController`.

`SlaveProvider` abstraction is such that a test case would simply request more slaves by calling
its `get()` method, and it's up to the implementation how this slave is provisioned and connected to Jenkins.

For example, one implementation might launch EC2 instance to run slaves in there, while another implementation
might just launch a slave locally on the same computer that the test harness is running (which happens to be the
default implementation `LocalSlaveProvider`.)

You can inject `SlaveProvider` to do this:

    @Inject SlaveProvider provider;

    @Test
    public void foo() {
        ...
        List<SlaveController> slaves = new ArrayList<>();
        for (int i=0; i<3; i++)
            slaves.add(provider.get());
        ...
    }

Or if your test case only need one, you can just inject SlaveController directly:

    @Inject SlaveController slave;

In both cases, slaves are automatically shut down at the end of a test.

## SlaveController
`SlaveProvider.get()` call will return `SlaveController`, which encapsulates the actual logic slave
instantiation, but this call by itself doesn't yet result in a Jenkins instance connected to the said slave.

That requires a separate call to the `install()` method:

    @Inject
    SlaveController sc;

    @Inject
    Jenkins jenkins;

    // create a new slave on the given Jenkins and wait for it to become online
    Slave s = sc.install(jenkins).get();

Some slave launch methods (such as JNLP slaves) allow slaves to be explicitly stopped without getting
automatically reconnected by Jenkins. The `stop()` and `start()` method provides these operations.
For other slave launch methods where Jenkins automatically tries to connect to a slave, these methods
are no-op.

This is not to be confused with the "marking a slave as temporarily offline" feature in Jenkins, which
belongs to the `Slave` page object.
