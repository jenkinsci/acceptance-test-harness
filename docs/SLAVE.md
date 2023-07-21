# Testing Agents
Just like [JenkinsController](CONTROLLER.md) allows test runners to test Jenkins with the environment of their choice,
this test harness allows test runners to control how slaves are set up to run tests.

For this purpose, we define `SlaveProvider` and `SlaveController`.

The `SlaveProvider` abstraction is such that a test case would simply request more agents by calling
its `get()` method, and it's up to the implementation how this agent is provisioned and connected to Jenkins.

For example, one implementation might launch an EC2 instance to run agents in there, while another implementation
might just launch an agent locally on the same computer that the test harness is running (which happens to be the
default implementation `LocalSlaveProvider`).

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

Or if your test case only needs one, you can just inject SlaveController directly:

    @Inject SlaveController slave;

In both cases, agents are automatically shut down at the end of a test.

## SlaveController
`SlaveProvider.get()` call will return `SlaveController`, which encapsulates the actual logic agent
instantiation, but this call by itself doesn't yet result in a Jenkins instance connected to the said agent.

That requires a separate call to the `install()` method:

    @Inject
    SlaveController sc;

    @Inject
    Jenkins jenkins;

    // create a new agent on the given Jenkins and wait for it to become online
    Slave s = sc.install(jenkins).get();

Some agent launch methods (such as JNLP agents) allow agents to be explicitly stopped without getting
automatically reconnected by Jenkins. The `stop()` and `start()` method provides these operations.
For other agent launch methods where Jenkins automatically tries to connect to an agent, these methods
are no-op.

This is not to be confused with the "marking an agent as temporarily offline" feature in Jenkins, which
belongs to the `Slave` page object.
