Feature: Allow publishing of Checkstyle report
  In order to be able to check code style of my project
  As a Jenkins user
  I want to be able to publish Checkstyle report

  Scenario: Record Checkstyle report
    Given I have installed the "checkstyle" plugin
    And a job
    When I configure the job
    And I copy resource "checkstyle_plugin/checkstyle-result.xml" into workspace
    And I add "Publish Checkstyle analysis results" post-build action
    And I set up "checkstyle-result.xml" as the Checkstyle results
    And I save the job
    And I build the job
    Then the build should have "Checkstyle Warnings" action
    And the job should have "Checkstyle Warnings" action

  Scenario: View Checkstyle report
    Given I have installed the "checkstyle" plugin
    And a job
    When I configure the job
    And I copy resource "checkstyle_plugin/checkstyle-result.xml" into workspace
    And I add "Publish Checkstyle analysis results" post-build action
    And I set up "checkstyle-result.xml" as the Checkstyle results
    And I save the job
    And I build the job
    Then the build should succeed
    When I visit Checkstyle report
    Then I should see there are 776 warnings
    And I should see there are 776 new warnings
    And I should see there are 0 fixed warnings
    And I should see there are 776 high priority warnings
    And I should see there are 0 normal priority warnings
    And I should see there are 0 low priority warnings
