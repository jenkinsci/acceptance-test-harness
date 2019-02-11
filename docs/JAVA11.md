# Running tests on JAVA11

To run the tests using a Java 11 virtual machine:

1. On the host, launch the container with Java11 VM:


   ```bash
   harry@devbox:~/acceptance-test-harness$ env java_version=11 ./ath-container.sh
   ```

1. In the container shell, set up the vnc:

   ```bash
   ath-user@1803848e337f:~/ath-sources$ eval $(./vnc.sh)
   ```

1. And run the tests with Java11 support:

   It depends on the Jenkins version to test. You can find the most updated documentation on how to run Jenkins on Java 11 at: https://jenkins.io/doc/administration/requirements/jenkins-on-java-11. Basically:

   For Jenkins [2.155, 2.163), add modules and _enable-future-java_:
   ```bash
   ath-user@1803848e337f:~/ath-sources$ env JAVA_OPTS=\"${JAVA_OPTS} -p /home/ath-user/jdk11-libs/jaxb-api.jar:/home/ath-user/jdk11-libs/javax.activation.jar --add-modules java.xml.bind,java.activation -cp /home/ath-user/jdk11-libs/jaxb-impl.jar:/home/ath-user/jdk11-libs/jaxb-core.jar\" JENKINS_OPTS=\"--enable-future-java\" ./run.sh ...
   ```

    For Jenkins 2.163, add _enable-future-java_:
   ```bash
   ath-user@1803848e337f:~/ath-sources$ JENKINS_OPTS=\"--enable-future-java\" ./run.sh ...
   ```

   From Jenkins 2.164 onwards:
   ```bash
   ath-user@1803848e337f:~/ath-sources$ ./run.sh ...

**NOTE**: Please take into account that it's an ongoing work, so it could be changed before long.
