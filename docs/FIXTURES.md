# Docker Test Fixtures

End-to-end testing requires a lot of non-trivial test fixtures that you want Jenkins to interact with,
such as SSH daemon, JIRA server, LDAP server, etc.

To allow us to define such fixtures in a portable and reusable manner, this test harness comes with
a mechanism and convention to define/use fixtures inside [Docker](http://docker.io/). They are
defined in `./fixtures/*`

##Docker injections

To control docker 2 injections are available.

    Docker = "docker";  // Name of the docker command
    dockerPortOffset = 40000; // Offset for binding the docker ports to an host ip address

Compare the dockerinject.groovy for a more advanced Docker injection.


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

The public function ipBound(n) and port(n) allow easily to find out to which host ip address and port a docker container
ip address is bound to.

## Writing a cucumber test that relies on Docker fixtures
`DockerSteps` defines steps that get the fixtures running, such as this:

    Given a docker fixture "tomcat7"

In Cucumber we refer to docker fixtures by their IDs, which is specified via `@DockerFixture` on the fixture type.
The containers are automatically terminated and cleaned up at the end of test.

## Defining a fixture
Each fixture is defined in terms of a `DockerContainer` subtype with `@DockerFixture` annotation. This type
exposes various methods needed to interact with the running fixture.

The `@DockerFixture` bindIp allows you to bind the accessible ports of the docker container to a local ip address.

A fixture also needs to define `Dockerfile` in the resources directory. If a fixture class is
`org/acme/FooContainer.java`, then the docker file must be located at `org/acme/FooContainer/Dockerfile`.


