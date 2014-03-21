# Machine configuration

You can launch pool of Machines each pool targeted to a Jenkins master or slave. Here you will create two subworlds, one for master and another one for slave.
See [Wiring](WIRING.md) for details on SubWorld and wiring different pieces.

## How to configure

As defined in [Wiring](WIRING.md), all the configuration parameters are Guice Named parameters. you should provide configuration in a groovy script.

## Ec2Provider configuration

See [EC2 Configuration](EC2-CONFIG.md).