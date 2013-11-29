Feature: Fixtures via docker
  To test Jenkins features that require non-trivial fixtures,
  this test uses Docker to launch a fixture without affecting the host environment.

  @native(docker)
  Scenario: Run an SSH server
    Given a docker fixture "sshd"
    Then I can login via ssh

