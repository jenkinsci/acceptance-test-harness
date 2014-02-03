Feature: Audit trail plugin

  Scenario: Trail should be empty after installation
    Given I have set up the Audit Trail plugin
    Then the audit trail should be empty

  Scenario: Trail should contain logged events
    Given I have set up the Audit Trail plugin
    When I create a job named "job"
    And  I create dumb slave named "slave"
    Then the audit trail should contain event "/createItem"
    And  the audit trail should contain event "/computer/createItem"
