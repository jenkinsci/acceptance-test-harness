Feature: Build freestyle jobs
  In order to configure and run simple shell scripts
  As a user
  I want to create jobs


  Scenario: Create a simple job
    Given a bare Jenkins instance
    When I create a job named "test"
    And I add a script build step to run "ls"
    And I run the job
    Then I should see console output matching "+ ls"

  Scenario: Disable a job
    Given a job
    When I configure the job
    And I click the "Disable Build" checkbox
    Then the job should be disabled

  Scenario: Enable concurrent builds
    Given a job
    When I configure the job
    And I click the "Execute Concurrent Builds" checkbox
    And I add a script build step to run "sleep 20"
    Then I should be able to build two jobs
    And the jobs should run concurrently

  Scenario: Create a parameterized job
    Given a job
    When I configure the job
    And I add a string parameter "Foo"
    When I try to build the job
    Then I should be prompted to enter the "Foo" parameter


# vim: tabstop=2 expandtab shiftwidth=2
