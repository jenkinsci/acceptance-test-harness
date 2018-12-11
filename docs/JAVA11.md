# Running tests on JAVA11

To run the tests using a Java 11 virtual machine:

1. On the host, launch the container with Java11 VM:

   ```bash
   harry@devbox:~/acceptance-test-harness$ env java_version=11 ./ath-container.sh
   ```

1. In the container shell, set up the vnc and run the tests with Java11 support:

   ```bash
   ath-user@1803848e337f:~/ath-sources$ eval $(./vnc.sh)
   ```

   ```bash
   ath-user@1803848e337f:~/ath-sources$ env JENKINS_OPTS="--enable-future-java" ./run.sh ...
   ```

**NOTE**: Please take into account that it's an ongoing work, so it could be changed before long.