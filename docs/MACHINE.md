# Introduction

[Machine](../src/main/java/org/jenkinsci/test/acceptance/machine/Machine.java) abstraction can be used to launch Jenkins
or Slave instances on a Machine. Machine is provisioned via
[MachineProvider](../src/main/java/org/jenkinsci/test/acceptance/machine/MachineProvider.java).

To install Jenkins instances on a Machine, you must provide a Guice binding of `JenkinsController` to
[JenkinsProvider](../src/main/java/org/jenkinsci/test/acceptance/machine/JenkinsProvider.java).
`Machine`s are injected into `JenkinsProvider`, `JenkinsProvider` in turn installs a Jenkins package on the Machine.
Jenkins installation is achieved by [JenkinsResolver](../src/main/java/org/jenkinsci/test/acceptance/resolver/JenkinsResolver.java) abstraction.

# MachineProviders

`MachineProvider` gives a new instance of `Machine`. A `MachineProvider` might give a Machine instance that was passivated
in the pool and is sanitized to be reused for the next Jenkins test run.


## JClouds API MachineProvider

[JcloudsMachineProvider](../src/main/java/org/jenkinsci/test/acceptance/machine/JcloudsMachineProvider.java) provides an abstract
implementation of [MachineProvider](../src/main/java/org/jenkinsci/test/acceptance/machine/JcloudsMachineProvider.java).
Any cloud specific Machine that JCloud API supports should extends from this class.
See [Ec2Provider](../src/main/java/org/jenkinsci/test/acceptance/machine/Ec2Provider.java),
it is a provider for EC2 Machines.


## EC2 Machine Provider

[Ec2Provider](../src/main/java/org/jenkinsci/test/acceptance/machine/Ec2Provider.java) provisions a Machine in AWS EC2.
It implements [JcloudsMachineProvider](../src/main/java/org/jenkinsci/test/acceptance/machine/JcloudsMachineProvider.java) abstraction.
Ec2 instances are configured with a default group name, and a set of port ranges (22 and 20000 to 21000) are opened for everyone with CIDR (0.0.0.0/0).
By default it launches an Ubuntu EBS volume based image on m1.small instance in us-east-1 region.

See [here](MACHINE-CONFIG.md) to change default EC2 configuration.

## Multi-tenant Machine Provider

Multi-tenant machines lives on a raw `Machine` instance. 'raw' Machine is a physical machine with a unique IP address.
At present multi-tenancy is achieved by locating each machine in its own file system directories with its own Jenkins
home, plugins, etc. At present all MT machines on a raw machine have same user, this can change in the future where another
implementation of MT Machine can be provisioned as chrooted or inside an lxc containers, etc.

The [MultitenancyMachineProvider](../src/main/java/org/jenkinsci/test/acceptance/machine/MultitenancyMachineProvider.java) is a
`MachineProvider` that gives instance of [MultiTenantMachine](../src/main/java/org/jenkinsci/test/acceptance/machine/MultiTentMachine.java).

To run Jenkins on EC2 Machine with multi-tenant Machines:

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

Just like JenkinsController, you can also inject `SlaveController`. See [SlaveController](AGENT.md) on how to bind Jenkins
masters and agents as different SubWorlds.

## Machine pooling

Machines provided by `JcloudsMachineProvider` or its subclasses are pooled.
Machines are returned by the JenkinsProvider or SlaveProvider after every test run.
By default the pool is started with just one machine on startup.
You can configure the pool size to be a different number.
If all pre-launched instances in the pool are in use the next `MachineProvider.get()`
  will launch a new instance of the machine.

# Machine grouping and caching

JClouds creates machine in a group.
`JcloudsMachineProvider` tells the cloud provider what group name under a pool of machine should be created.
We create unique group name, it's based on the checksum of username, local host name/IP address and checksum of config file.
On the test re-runs we re-use the machines who are running under the same group name that they were created under.
Of course if there is change in configuration or you run the tests from different computer new set of machine instances will be created.

## Machine self-shutdown

We install an auto-terminate script that monitors inactivity for for a given time and if no activity found, it initiates termination.
Inactivity is determined by max up to 1 ssh connection to machine for 3 hours.

**Note** According to Amazon [doc on instance initiated shutdown behavior], it works as long as the instance is EBS based.
Default AMI image is EBS based. If you are using a different AMI make sure it's EBS based for this feature to work.


# Machine configuration
[See here](MACHINE-CONFIG.md)
