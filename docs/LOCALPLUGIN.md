# Testing Unreleased Plugin

When tests require the presence of plugins, by default the harness will install necessary plugins from
the update center.

When you are testing locally developed Jenkins plugin, you'd like the test harness to pick up your
local version as opposed to download the plugin from update center. This can be done by instructing the harness
accordingly.

One way to do so is to set the environment variable:

    $ export LDAP.JPI=/path/to/your/ldap.jpi
    $ mvn test            // run the tests

You can also do this from [the groovy wiring script](WIRING.md).

    envs['LDAP.JPI'] = '/path/to/your.ldap.jpi'

TODO: provide a better binding for this