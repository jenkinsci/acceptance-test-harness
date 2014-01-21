Feature: Sonar
  @native(docker)
  Scenario: Sonar
    Given a docker fixture "sonar"
    When I visit sonar
    Then the build should succeed
