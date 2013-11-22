Feature: Update JIRA tickets when a build is ready
  In order to notify people waiting for a bug fix
  As a Jenkins developer
  I want JIRA issues to be updated when a new build is made

  Scenario: JIRA ticket gets updated with a build link
    Given a docker fixture "jira"
    And "ABC" project on docker jira fixture
    And I have installed the "jira" plugin
    Then I will write the rest of the test
