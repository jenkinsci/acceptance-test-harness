# Managing the versions of Jenkins and plugins

## Jenkins

Jenkins accepts `JENKINS_WAR` parameter to provide a local Jenkins war file to use. Alternatively, `JENKINS_VERSION` can be used to specify a version (like 1.625.3) to be downloaded and used instead.

## Plugins

When tests require the presence of plugins, by default the harness will install necessary plugins from
the update center. There are several ways to override this:

### Use custom plugin version

Environment variables like `<ARTIFACT_ID>.version` can be used to specify what version will be installed:

    $ env git.version=2.3 mvn test

### Use custom plugin file

When you are testing locally developed Jenkins plugin, you'd like the test harness to pick up your
local version as opposed to download the plugin from update center. This can be done by instructing the harness
accordingly.

One way to do so is to set the environment variable:

    $ env ldap.jpi=/path/to/your/ldap.jpi mvn test

You can also do this from [the groovy wiring script](WIRING.md).

    envs['ldap.jpi'] = '/path/to/your.ldap.jpi'

Since newer shells do not support environment variables with hyphens in the name, rather than `scm-api.jpi` you may use `SCM_API_JPI`.

### Install plugins from local maven repository

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

### Provide full plugins collection

Contrary to previous ways to alter the plugin versions used, `PLUGINS_DIR` 
environment variable specifies a path to a directory with plugins that will all be 
installed into Jenkins for every test. If test require some plugin that is not part of the collection, 
it will be installed from update center. This is useful to verify whether particular set of plugins works 
as expected (and it is also a good way to speed up your tests since no slow plugin installations are 
executed during your tests).

Note that this option is not yet supported by all [Jenkins controllers](CONTROLLER.md), 
so e.g. use `TYPE=winstone` in order to get the `PLUGINS_DIR` option working.