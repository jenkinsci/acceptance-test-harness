Feature: Priority Sorter Plugin

  Background:
    Given I have installed the "PrioritySorter" plugin
    And a dumb slave
    And I restart Jenkins

  Scenario: Match jobs by name
    When I configure absolute sorting strategy with 2 priorities
    And I set priority 2 for job "low_priority"
    And I set priority 1 for job "high_priority"

    And I create a job named "low_priority"
    And I tie the job to the "slave" label
    And I queue a build
    And I create a job named "high_priority"
    And I tie the job to the "slave" label
    And I queue a build

    And I add the label "slave" to the slave
    Then the build should succeed
    And jobs should be executed in order on the slave
        | high_priority | low_priority |

  Scenario: Match jobs by view
    When I configure absolute sorting strategy with 2 priorities

    And I set priority 2 for view "normal"
    And I create a view named "normal"
    And I create job "P2" in the view
    And I tie the job to the "slave" label
    And I queue a build

    And I set priority 1 for view "prioritized"
    And I create a view named "prioritized"
    And I create job "P1" in the view
    And I tie the job to the "slave" label
    And I queue a build

    And I add the label "slave" to the slave
    Then the build should succeed
    And jobs should be executed in order on the slave
        | P1 | P2 |
