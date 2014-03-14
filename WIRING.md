# Configuring how to run tests

`README.md` discusses how you can change the way tests run and what you test by using several environment variables.
While configuring test runs through environment variables is simple, it only works for simple cases.

For more sophisticated test runs (for example if you want your Jenkins-under-test and slaves to both run on EC2
but on different kinds of machines), you can do the wiring by writing a Groovy script that directly defines
component bindings in Guice.

This functionality is built on top of [https://github.com/jenkinsci/lib-groovy-guice-binder](Groovy Guice Binder),
so it allows you to write something like this:

    // run tests by launching "java -jar jenkins.war" on the local computer as Jenkins-under-test
    bind JenkinsController toInstance new WinstoneController("/path/to/my/jenkins.war)

Or:

    // run tests by launching EC2 instances and run "java -jar jenkins.war" in there.
    ec2ConfigFile = "/home/you/ec2config"
    bind MachineProvider to Ec2Provider
    bind JenkinsController toProvider JenkinsProvider in TestScope

    // allocate slaves from the above MachineProvider, then attach them to Jenkins
    // as SSH slaves
    bind SlaveProvider to SshSlaveProvider


### Sub-worlds
In the last example above, both Jenkins masters and slaves come from the same EC2 image types,
because they both use the same `MachineProvider` to acquire machines.

Sometimes this is not sufficient, and one would want to configure two sets of `Ec2Provider`.
You can do this by creating two "sub-worlds", which are really independent Guice injectors.

    // create an entirely independent guice injector named "master"
    master = subworld {
        // use the same groovy guice binder DSL to define bindings in the sub-world
        ec2ConfigFile = "/home/you/ec2config-for-masters"
        bind MachineProvider to Ec2Provider
        bind JenkinsController toProvider JenkinsProvider in TestScope
    }

    // ditto for slaves
    slaves = subworld {
        // bindings you define here are independent of those you define in the master sub-world above.
        ec2ConfigFile = "/home/you/ec2config-for-slaves"
        bind MachineProvider to Ec2Provider
    }

    // then alias JenkinsController to the one defined in the master sub-world
    bind JenkinsController toProvider master[JenkinsController]

    // ditto
    bind SlaveProvider toProvider slaves[SshSlaveProvider]
