Feature: Adds Apache Maven support
  In order to be able to build Maven projects
  As a Jenkins user
  I want to install and configure Maven and build Maven based project

  Scenario: Build multimodule Maven project
    Given I have default Maven configured
    And a Maven job
    When I configure the job
    And I copy resource "maven/repositories/multimodule/*" into workspace
    And I add a top-level maven target "package"
    And I save the job
    And I build the job
    Then the build should succeed
    And the job should have "Modules" action
    And console output should contain "Building root 1.0"
    And console output should contain "Building module_a 2.0"
    And console output should contain "Building module_b 3.0"

  @bug(17713)
  Scenario: Display job modules for Maven project
    Given I have default Maven configured
    And a Maven job
    When I configure the job
    And I copy resource "maven/repositories/multimodule/*" into workspace
    And I add a top-level maven target "package"
    And I save the job
    And I build the job
    Then the job should have module named "gid:root"
    Then the job should have module named "gid:module_a"
    Then the job should have module named "gid:module_b"
