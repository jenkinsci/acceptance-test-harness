Feature: Configure/build freestyle jobs
  In order to get some basic usage out of freestyle jobs
  As a user
  I want to configure and run a series of different freestyle-based jobs

  Scenario: Create a simple job
    When I create a job named "MAGICJOB"
    And I visit the home page
    Then the page should say "MAGICJOB"

  Scenario: Run a simple job
    Given a job
    When I configure the job
    And I add a script build step to run "ls"
    And I save the job
    And I run the job
    Then I should see console output matching "+ ls"

  Scenario: Disable a job
    Given a job
    When I configure the job
    And I check the "disable" checkbox
    And I save the job
    And I visit the job page
    Then the page should say "This project is currently disabled"

  Scenario: Enable concurrent builds
    Given a job
    When I configure the job
    And I enable concurrent builds
    And I add a script build step to run "sleep 20"
    And I save the job
    And I build 2 jobs
    Then the 2 jobs should run concurrently

  Scenario: Create a parameterized job
    Given a job
    When I configure the job
    And I add a string parameter "Foo"
    And I run the job
    Then I should be prompted to enter the "Foo" parameter

  Scenario: Disable a job
    Given a job
    When I disable the job
    Then it should be disabled
    And it should have an "Enable" button on the job page

  Scenario: Old build should be discarted
    Given a simple job
    When I configure the job
    And I set 2 builds to keep
    And I save the job
    And I build 4 jobs
    Then the job should not have build 1
    And  the job should not have build 2
    And  the job should have build 3
    And  the job should have build 4
    And  the job should not have build 5

  Scenario: Do not discard build kept forever
    Given a simple job
    When I configure the job
    And I set 1 build to keep
    And I save the job
    And I run the job
    And I lock the build
    And I build 2 jobs
    Then the job should have build 1
    And  the job should not have build 2
    And  the job should have build 3
    And  the job should not have build 4

  Scenario: Do not discard last successfull build
    Given a simple job
    When I configure the job
    And I set 1 build to keep
    And I save the job
    And I run the job
    And I configure the job
    And I add always fail build step
    And I save the job
    And I build 2 jobs
    Then the job should have build 1
    And  the job should not have build 2
    And  the job should have build 3
    And  the job should not have build 4

# vim: tabstop=2 expandtab shiftwidth=2
