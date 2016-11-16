# Docker Test Fixtures

**Mac OS X Note:** To execute tests on Mac OS X please read about [Docker on Mac OS X](./DOCKER_OSX.md) first.

End-to-end testing requires a lot of non-trivial test fixtures that you want Jenkins to interact with,
such as SSH daemon, JIRA server, LDAP server, etc.

To allow us to define such fixtures in a portable and reusable manner, this test harness comes with
a mechanism and convention to define/use fixtures inside [Docker](http://docker.io/). They are
defined in `./fixtures/*`

## Running/skipping Docker tests

JUnit Tests that require docker fixtures are marked with `@WithDocker` annotation.
Cucumber tests are likewise annotated with `@native(docker)` annotation.
If docker is not installed when you run tests, these tests are automatically skipped.

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

In case of inner classes, dollar sign ( $ ) is replaced with a slash ( / ).

You can also set custom location of `Dockerfile` by using `dockerfileFolder` attribute from`@DockerFixture` annotation.
A simple example might be:

```java
@DockerFixture(id="test", ports="8080", dockerfileFolder="org/acme/fixture/test")
public class TestContainer extends DockerContainer {
//...
}
```

In this case the docker file must be located at classpath and in `org/ame/fixture/test` instead of the default location.

## Running the ATH itself as a docker container
Depending on the CI infrastructure setup one may need to run the ATH itself in a docker container with access to the host docker service,
following a strategy similar to the one described in [this article](http://jpetazzo.github.io/2015/09/03/do-not-use-docker-in-docker-for-ci/).

When this is needed, the docker fixtures should not be accessed through mapped ports on the docker host, but directly
through their container IP and port since they are "sibling" containers to the ATH.

To enable this, set the environment variable `SHARED_DOCKER_SERVICE=true`, and then the functions ipBound(n) and port(n)
will just return the container's IP and port where the fixture can be accessed.

TO-DO: Instead of depending on setting this environment variable, in the future we could try to somehow automatically detect the situation.
