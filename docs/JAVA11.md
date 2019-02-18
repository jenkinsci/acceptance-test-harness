# Running tests on JAVA11

To run the tests using a Java 11 virtual machine:

1. On the host, launch the container with Java11 VM:

   ```bash
   harry@devbox:~/acceptance-test-harness$ env java_version=11 ./ath-container.sh
   ```

1. In the container shell, act as usual, set up the vnc:

   ```bash
   ath-user@1803848e337f:~/ath-sources$ eval $(vnc.sh)
   ```

1. And run the tests with Java11 support:
   ```bash
   ath-user@1803848e337f:~/ath-sources$ run.sh ...
   ```

**Note**: It was possible to run Jenkins on Java 11 since the version `2.155`, but its usage was then more complex. We do not recommend to use any version prior to `2.164`.
To have up-to-date information see https://jenkins.io/doc/administration/requirements/jenkins-on-java-11
