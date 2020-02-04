# Using ATH in plugin codebase

You may wish to write Selenium-based UI tests that are not kept in this repository.
For example, you may have proprietary plugins which you plan to test using this harness or that the plugin does not comply with [contributing guidelines](CONTRIBUTING.md).
This is easily accomplished.

## Basic setup

Acceptance test harness in known not to coexist well with Jenkins plugin sources. That is why it is recommended to split
the sources to plugin and UI tests to individual maven modules. 

```xml
    <modules>
        <module>plugin</module>
        <module>ui-tests</module>
    </modules>
```

The `plugin` would be an ordinary Jenkins plugin module, while the `ui-tests` will contain the dependency to `acceptance-test-harness` and you UI tests.
Now just create page objects in `ui-tests/src/main/java/`, tests in `ui-tests/src/test/java/`, and other classes and resources as usual.
You can of course reuse `AbstractJUnitTest`, page objects, fixtures, etc. from the OSS test harness.

The tests themselves will be executed by (in `ui-tests/pom.xml`):

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <version>3.0.0-M4</version>
    <configuration>
        <reuseForks>false</reuseForks>
        <environmentVariables><!-- read by ATH -->
            <!-- Jenkins version to be used for ATH run -->
            <JENKINS_VERSION>${jenkins.version}</JENKINS_VERSION>
            <!-- Use local version of the plugin over the released one ATH would otherwise use-->
            <LOCAL_JARS>../plugin/target/XXX.hpi</LOCAL_JARS>
            <!-- Run the web browser in a container -->
            <BROWSER>firefox-container</BROWSER>
        </environmentVariables>
    </configuration>
</plugin>
```

Other configuration environment variables for ATH can be used as well - not all make sense in this use case, though.

It is advised not to release the aggregating parent and the ui-tests module. Also, the tests in `ui-tests` cannot work on
Windows so they need to be skipped conditionally.

See existing plugins to be used as an example:

- [openstack-cloud](https://github.com/jenkinsci/openstack-cloud-plugin/)
- [kerberos-sso](https://github.com/jenkinsci/kerberos-sso-plugin/)
