Feature: configure slaves
  In order to effectively use more machines
  As a user
  I want to be able to configure slaves and jobs to distribute load

  Scenario: Tie a job to a specified label
    Given a job
    And a dumb slave
    When I add the label "test" to the slave
    And I configure the job
    And I tie the job to the "test" label
    And I build the job
    Then the job should be tied to the "test" label
    And the build should run on the slave

  Scenario: Tie a job to a specific slave
    Given a job
    And a dumb slave
    When I configure the job
    And I tie the job to the slave
    And I build the job
    Then the job should be tied to the slave
    And the build should run on the slave

  Scenario: Create a slave with multiple executors
    Given a dumb slave
    When I set the executors to "3"
    Then I should see "3" executors configured
