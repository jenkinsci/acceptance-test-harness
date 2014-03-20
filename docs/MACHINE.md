# Introduction

[Machine](../src/main/java/org/jenkinsci/test/acceptance/machine/Machine.java) abstraction can be used to launch Jenkins
or Slave instances on a Machine. Machine is provisioned via
[MachineProvider](../src/main/java/org/jenkinsci/test/acceptance/machine/MachineProvider.java).

To install Jenkins instances on a Machine, you must provider Guice binding of `JenkinsController` to
[JenkinsProvider](../src/main/java/org/jenkinsci/test/acceptance/machine/JenkinsProvider.java). `Machine`s are injected in to
`JenkinsProvider`, JenkinsProvider in turn installs a Jenkins package on the Machine. Jenkins installation is achieved by
[JenkinsResolver](../src/main/java/org/jenkinsci/test/acceptance/resolver/JenkinsResolver.java) abstraction.

# MachineProviders

`MachineProvider` gives a new instance of `Machine`. A MachineProvider might give a Machine instance that was passivated
in the pool and is sanitized to be reused for the next Jenkins test run.

## EC2 Machine Provider

[Ec2Provider](../src/main/java/org/jenkinsci/test/acceptance/machine/Ec2Provider.java) provisions a Machine in AWS EC2.
It implements [JcloudsMachineProvider](../src/main/java/org/jenkinsci/test/acceptance/machine/JcloudsMachineProvider.java) abstraction.
`JcloudsMachineProvider` is a abstract class implements machine pooling, caching, bootup and termination logic. For
machine provisioning it uses [JCloud API](https://github.com/jclouds/jclouds). To add `MachineProvider` for other clouds
such as Rackspace etc. you need to provide implementation of `JcloudsMachineProvider`.

## Multi-tenant Machine Provider

Mutli-tenant machines lives on a raw `Machine` instance. 'raw' Machine is a physical machine with a unique IP address.
At present multi-tenancy is achieved by locating each machine in it's own file system directories with it's own Jenkins
home, plugins etc. At present all MT machines on a raw machine have same user, this can change in future where another
implementation of MT Machine can be provisioned as chrooted or inside an lxc containers etc.

The [MultitenancyMachineProvider](../src/main/java/org/jenkinsci/test/acceptance/machine/MultitenancyMachineProvider.java) is a
`MachineProvider` that gives instance of [MultiTenantMachine](../src/main/java/org/jenkinsci/test/acceptance/machine/MultiTentMachine.java).

To run Jenkins on EC2 Machine with multi-tenant Machines

    // in your groovy binding configuration file or a Guice binding module

    //this binding will inject MultiTenantMachine anywhere you have @inject Machine in your code

    bind MachineProvider to MultitenancyMachineProvider
    bind MachineProvider named "raw" to Ec2Provider

    // This will inject `RemoteJenkinsController` for @Inject JenkinsController. Each `JenkinsController` will install
    // Jenkins on a MultiTenantMachine

    bind JenkinsController toProvider JenkinsProvider

    //In your test
    @Inject JenkinsController jenkinsController;

    @Test
    public void foo() {
        ...
        jenkinsController.start();
        ...
    }

Just like JenkinsController, you can also inject `SlaveController`. See [SlaveController](SLAVE.md) on how to bind Jenkins
masters and slaves as different SubWorlds.