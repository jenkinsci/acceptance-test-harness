Feature: Allow publishing of Cobertura analysis
  In order to be able track test coverage of my project
  As a Jenkins user
  I want to be able to publish Cobertura analysis report

  @realupdatecenter
  Scenario: Install Cobertura plugin
    Given I have installed the "cobertura" plugin
    And a job
    Then I should be able to use "Publish Cobertura Coverage Report" post-build action

  @realupdatecenter
  Scenario: Record Cobertura coverage report
    Given I have installed the "cobertura" plugin
    And a job
    When I configure the job
    And I copy resource "cobertura_plugin/coverage.xml" into workspace as "coverage.xml" via shell command
    And I add "Publish Cobertura Coverage Report" post-build action
    And I set up "coverage.xml" as the Cobertura report
    And I save the job
    And I build the job
    Then the build should have "Coverage Report" action
    And the job should have "Coverage Report" action
