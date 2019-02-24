# EC2 MachineProvider configuration

[Ec2Config]((../src/main/java/org/jenkinsci/test/acceptance/machine/Ec2Config.java)) is used to configure EC2 instances.


## EC2 configuration

### EC2 credentials setup

EC2 credentials (`aws_access_key_id` and `aws_secret_access_key`) are picked up from `~/.aws/config` file's `default` section.
This file is automatically created for you with your ec2 credentials if you installed [AWS CLI](http://aws.amazon.com/cli/).

Make sure your keys are valid by running:

    $ ec2 describe-instances


You can also create this file manually, it must be of this format:

    [default]
    output = json
    region = us-east-1
    aws_access_key_id = xxxxxxx
    aws_secret_access_key = xxxxxxxxxx

### EC2 instance configuration

You can customize EC2 instance configuration. Here are list of parameters in groovy script format:

    ec2ConfigFile = "~/.aws/config" //default ec2 config location
    user="ubuntu" //default user on remote machine
    profile="default" // default EC2 config file section.
    region="us-east-1"
    instanceType="m1.small"
    imageId="ami-350c295c" //Ubuntu based EBS root volume instance
    inboundPortRange="20000..21000" //inbound port range on EC2 machine. All the ports from 20000 to 21000 are granted
                                    //permission for access by all.

### Authenticator configuration to access instance by harness

Test harness needs to install Jenkins, runs processes and do bunch of other things on the EC2 instance.
This needs an authenticator to be configured.
Here is the configuration that should go in to your groovy script file:

    bind Authenticator named "publicKeyAuthenticator" to PublicKeyAuthenticator

### Multi-tenanting EC2 instance

The following configuration is needed to multi-tenant an ec2 instance:

    maxNumOfMachines=1 //How many EC2 instances to launch at startup
    maxMtMachines=2    //How many MT machine to fit in each EC2 instances


    // Jenkins and Slaves will be injected with MultitenancyMachineProvider
    // This means Jenkins and slaves will be runnning on an MT machine
    bind MachineProvider to MultitenancyMachineProvider

    // Inject MultitenancyMachineProvider with EC2Provider to give it a base Machine
    bind MachineProvider named "raw" to EC2Provider

`MultitenancyMachineProvider` does multi-tenanting on a **base** or **raw** Machine. This base machine is instance of a
machine running in the cloud.
