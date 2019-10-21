# How to use this from your own module

You may wish to write Jenkins-based acceptance tests that are not kept in this repository.
For example, you may have proprietary plugins which you plan to test using this harness.
This is easily accomplished.

## Basic setup

Just create a Maven project (with the default `jar` packaging) depending on the harness:

```
  <dependencies>
    <dependency>
      <groupId>org.jenkins-ci</groupId>
      <artifactId>acceptance-test-harness</artifactId>
      <version>…</version>
    </dependency>
  </dependencies>
```

You may want to specify Java 8 or even 11 sources:

```
  <build>
    <plugins>
      <plugin>
        <artifactId>maven-compiler-plugin</artifactId>
        <configuration>
          <source>1.8</source>
          <target>1.8</target>
        </configuration>
      </plugin>
    </plugins>
  </build>
```

Now just create page objects in `src/main/java/`, tests in `src/test/java/`, and other classes and resources as usual.
You can of course reuse `AbstractJUnitTest`, page objects, fixtures, etc. from the OSS test harness.

## JUT server

If you would like to use the [JUT server](PRELAUNCH.md), add this snippet:

```
  <build>
    <plugins>
      <plugin>
        <groupId>org.codehaus.mojo</groupId>
        <artifactId>appassembler-maven-plugin</artifactId>
        <version>1.8</version>
        <executions>
          <execution>
            <phase>package</phase>
            <goals>
              <goal>assemble</goal>
            </goals>
          </execution>
        </executions>
        <configuration>
          <programs>
            <program>
              <mainClass>org.jenkinsci.test.acceptance.server.JenkinsControllerPoolProcess</mainClass>
              <id>jut-server</id>
            </program>
          </programs>
        </configuration>
      </plugin>
    </plugins>
  </build>
```

And create a `jut-server.sh` in the root of your project:

    #!/bin/bash
    DIR="$( cd "$( dirname "$0" )" && pwd )"
    CMD="$DIR/target/appassembler/bin/jut-server"
    if [ ! -s $CMD ]
    then
      mvn package -DskipTests -f "$DIR/pom.xml"
    fi
    sh "$CMD" "$@"

## Controlling test output

The following profile is recommended:

```
  <profiles>
    <profile>
      <id>all-tests</id>
      <activation>
        <property>
          <name>!test</name>
        </property>
      </activation>
      <properties>
        <maven.test.redirectTestOutputToFile>true</maven.test.redirectTestOutputToFile>
      </properties>
    </profile>
  </profiles>
```

This ensures that test output is printed to standard output when running a single test, typically while under development;
yet output is suppressed when running _all_ tests, when it would be noisy (especially when using multiple threads).

## Releasing project versions

If you plan to use `maven-release-plugin` on your own project for some reason, you may add:

```
  <build>
    <plugins>
      <plugin>
        <artifactId>maven-release-plugin</artifactId>
        <configuration>
          <arguments>-DskipTests</arguments>
        </configuration>
      </plugin>
    </plugins>
  </build>
```

... since you would not want to run acceptance tests during the release.
(Without some environment variables they would fail anyway.)

## Selecting the browser and Jenkins WAR from Maven profiles

If you like to activate Maven profiles from `~/.m2/settings.xml` with `-P` to run with specific environments, try:

```
  <profiles>
    <profile>
      <id>BROWSER</id>
      <activation>
        <property>
          <name>BROWSER</name>
        </property>
      </activation>
      <build>
        <plugins>
          <plugin>
            <artifactId>maven-surefire-plugin</artifactId>
            <configuration>
              <environmentVariables>
                <BROWSER>${BROWSER}</BROWSER>
              </environmentVariables>
            </configuration>
          </plugin>
        </plugins>
      </build>
    </profile>
    <profile>
      <id>JENKINS_WAR</id>
      <activation>
        <property>
          <name>JENKINS_WAR</name>
        </property>
      </activation>
      <build>
        <plugins>
          <plugin>
            <artifactId>maven-surefire-plugin</artifactId>
            <configuration>
              <environmentVariables>
                <JENKINS_WAR>${JENKINS_WAR}</JENKINS_WAR>
              </environmentVariables>
            </configuration>
          </plugin>
        </plugins>
      </build>
    </profile>
  </profiles>
```
