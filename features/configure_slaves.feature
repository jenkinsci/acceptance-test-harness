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
    Then I should see the job tied to the "test" label

  Scenario: Tie a job to a specific slave
    Given a job
    And a dumb slave
    When I configure the job
    And I tie the job to the slave
    Then I should see the job tied to the slave


# vim: tabstop=2 expandtab shiftwidth=2
