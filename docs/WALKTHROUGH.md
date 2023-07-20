# Walkthrough of running ATH tests on changes made to a local version of Jenkins

In this walkthrough I am going to show you how to locally use this ATH to test changes made in a local copy of Jenkins.

## Getting Started

In this walkthrough I will be using Docker, IntelliJ and a terminal.

1) Make your own fork of https://github.com/jenkinsci/jenkins
2) Clone the fork to your local machine
> **Note** The next two steps are optional, this makes it easy to make a PR to Jenkins if you wish to do so later
3) Create a new branch and give it a sensible name
4) Make your changes to your copy of Jenkins, commit changes to your branch, feel free to push your changes to your local fork 
5) When you have made changes to your local copy of Jenkins, in a terminal window `cd` into your local copy of Jenkins and run `mvn verify`, this will run the integration tests inside the jenkins repo. 


## Running the ATH

When you are ready to test your changes with this ATH, you will need to run `mvn package` on your local copy of Jenkins. Navigate in your local Jenkins repo to `…\war\target` and you will see a war file for you to use later.

### Testing with this ATH
1) Clone a copy of this ATH repo to your local machine and `cd` into the directory

> **Note:** There are many ways to run these tests, I will be running the tests using Windows, your commands may differ slightly if using a different OS. See the [README.md](README.md) for alternatives and configuration options.  

2) In a terminal window type `set JENKINS_WAR=C:\Users\julie\jenkins\war\target\jenkins.war` This sets a new environment variable called `JENKINS_WAR` to the path of your copy of the jenkins war *(You created this in the `mvn package step`, change it to your directory path instead of mine)*
3) Type `vars.cmd` to the same terminal window. This is a script for setting all the variables in order to run the ATH locally on windows, it also outputs a docker command. 
4) Copy the docker command that was output to your terminal after the last step, and paste it into the same terminal window. This will run the docker container with your local copy of Jenkins.

**You are now ready to run tests.**

If you want to run all the tests, run `mvn test`, if you want to run a specific test run `mvn test -Dtest=ArtifactsTest` *(change the test name to the one you wish to run)* tests can be found in the src/test/java directory of this ATH repository.

### Tips and tricks
- When running your tests with `mvn test`, adding the parameter `-DforkCount=` with a number higher than 1 will run tests in parallel. This is useful for running tests in a shorter amount of time. See [here](https://maven.apache.org/surefire/maven-surefire-plugin/examples/fork-options-and-parallel-execution.html) for more details.
- If you make changes to your local jenkins version after running ATH tests, and you want to re-run the tests again, make sure you run `mvn package` again in your local jenkins repository

## Debugging
> **Note:** there are lots of good IDEs out there, I will be using IntelliJ for this walkthrough.

1) Open the ATH in Intellj and add your breakpoints to the test(s) you want to debug.
2) Click 'edit configuration' and add a new ‘Remote JVM Debug’
3) Check these settings:
   - Debugger mode = `Attach to remote JVM`
   - Transport = `Socket`
   - Host = `localhost`
   - Port = `5005`
   - Command line arguments for remote JVM = `-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005`
   - Click apply and ok 
 
Now go back to your terminal window which is pointed at the acceptance test local repo. 

4) I want to debug the `InstallWizardTest`, so I type into the terminal `mvn test -Dtest=InstallWizardTest -Dmaven.surefire.debug`
5) As soon as you click the debug button on your intellij window, the test will run and stop at your breakpoints.

For more information on maven debugging, see [here](https://maven.apache.org/surefire/maven-surefire-plugin/examples/debugging.html).


