# Docker Test Fixtures

End-to-end testing requires a lot of non-trivial test fixtures that you want Jenkins to interact with,
such as SSH daemon, JIRA server, LDAP server, etc.

To allow us to define such fixtures in a portable and reusable manner, this test harness comes with
a mechanism and convention to define/use fixtures inside [Docker](http://docker.io/). They are
defined in `./fixtures/*`


## Running/skipping Docker tests
Tests that require docker fixtures are marked with `@docker` annotation. If docker is not installed
when you run tests, these tests are automatically skipped.


## Writing a cucumber test that relies on Docker fixtures
`docker_steps.rb` defines steps that get the fixtures running, such as this:

    Given a docker fixture "tomcat7"

The instantiated fixture object is assigned to `@docker["tomcat7"]` so that your step definitions can access them.
See `deploy_steps.rb` for an example of interacting with running containers.


## Defining a fixture
Each fixture is defined in a directory named after its ID. A fixture minimally consists
of `Dockerfile` that defines how to build the image, and it also must accompany `ID.rb` file
that defines a fixture wrapper class that encapsulates interactions to the container.

The fixture wrapper class extends from `Fixture` class, and it needs to call `register` class method
to make the wrapper class discoverable to the runtime.

`Jenkins::Fixtures::Fixture.search_path` is a string array that lists directories that contain fixtures.
This allows you to define your local fixtures elsewhere, for example if you have your own in-house acceptance testing.