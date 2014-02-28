Feature: Timestamper support
  In order to have more informative build logs
  As a Jenkins user
  I want to decorate console log entries with timestamps

  Background:
    Given I have installed the "timestamper" plugin
    And a job inserting timestamps

  Scenario: Display no timestamps
    When I build the job
    And I select no timestamps
    Then there are no timestamps in the console

  Scenario: Display system time timestamps
    When I build the job
    And I select system time timestamps
    Then console timestamps matches regexp "\d\d:\d\d:\d\d"

  Scenario: Display elapsed time timestamps
    When I build the job
    And I select elapsed time timestamps
    Then console timestamps matches regexp "\d\d:\d\d:\d\d.\d\d\d"

  Scenario: Display specific system time timestamps
    When I set "'At 'HH:mm:ss' system time'" as system time timestamp format
    And I build the job
    And I select system time timestamps
    Then console timestamps matches regexp "At \d\d:\d\d:\d\d system time"

  Scenario: Display specific elapsed time timestamps
    When I set "'Exactly 'HH:mm:ss.S' after launch'" as elapsed time timestamp format
    And I build the job
    And I select elapsed time timestamps
    Then console timestamps matches regexp "Exactly \d\d:\d\d:\d\d.\d\d\d after launch"
