Feature: Build freestyle jobs
  In order to configure and run simple shell scripts
  As a user
  I want to create jobs


  Scenario: Create a simple job
    Given a Jenkins instance
    When I create a job named "test"
    And I add a script build step to run "ls"
    And I run the job
    Then I should see console output matching "+ ls"

# vim: tabstop=2 expandtab shiftwidth=2
