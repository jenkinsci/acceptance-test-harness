# Testing Unreleased Plugin

When tests require the presence of plugins, by default the harness will install necessary plugins from
the update center.

When you are testing locally developed Jenkins plugin, you'd like the test harness to pick up your
local version as opposed to download the plugin from update center. This can be done by instructing the harness
accordingly.

One way to do so is to set the environment variable:

    $ export ldap.jpi=/path/to/your/ldap.jpi
    $ mvn test            // run the tests

You can also do this from [the groovy wiring script](WIRING.md).

    envs['ldap.jpi'] = '/path/to/your.ldap.jpi'

TODO: provide a better binding for this

As a convenience, you can also run with the variable `LOCAL_SNAPSHOTS=true`.
This will override any plugin in the update center with a locally built `SNAPSHOT` version (if newer than the released one).
You can leave this mode enabled and thus not need to specify a new environment variable for each plugin you are testing.
For example, you add to your `~/.m2/settings.xml` a profile like

    <profile>
        <id>acceptance-tests</id>
        <activation>
            <activeByDefault>true</activeByDefault>
        </activation>
        <properties>
            <JENKINS_WAR>/…/jenkinsci/jenkins/war/target/jenkins.war</JENKINS_WAR>
            <LOCAL_SNAPSHOTS>true</LOCAL_SNAPSHOTS>
        </properties>
    </profile>

and then `mvn -DskipTests install` on Jenkins core or any plugin before running acceptance tests.
