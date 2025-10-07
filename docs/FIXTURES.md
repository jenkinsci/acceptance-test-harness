# Docker Test Fixtures

End-to-end testing requires a lot of non-trivial test fixtures that you want Jenkins to interact with,
such as an SSH daemon, a JIRA server, an LDAP server, etc.

To allow us to define such fixtures in a portable and reusable manner, this test harness comes with
a mechanism and convention to define/use fixtures inside [Docker](http://docker.io/). They are
defined in `./fixtures/*`

## General instructions

See the [documentation for the `docker-fixtures` library](https://github.com/jenkinsci/docker-fixtures/blob/master/README.md#usage) for general usage.
Acceptance tests however use a different injection style rather than `DockerRule`; see below.

## Running/skipping Docker tests

JUnit Tests that require docker fixtures are marked with `@WithDocker` annotation.
If docker is not installed when you run tests, these tests are automatically skipped.

## Docker injections

To control docker 2 injections are available.

    Docker = "docker";  // Name of the docker command
    dockerPortOffset = 40000; // Offset for binding the docker ports to an host ip address


## Writing a JUnit test that relies on Docker fixtures
Every fixture has a subtype of `DockerContainer` defined for it (see
[existing examples](../src/main/java/org/jenkinsci/test/acceptance/docker/fixtures/))

Pick the fixture type you want to use, then insert `DockerContainerHolder` for it:

    @Inject
    DockerContainerHolder<Tomcat7Container> tomcat7;

    @Test
    public void myTomcat7Test() {
        Tomcat7Container c = tomcat7.get();
        // interact with 'c' during test
    }

`DockerContainerHolder` starts a container, and it'll automatically clean-up the container at the end of the test.
