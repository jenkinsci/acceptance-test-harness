Feature: Allow publishing of Cobertura analysis
  In order to be able track test coverage of my project
  As a Jenkins user
  I want to be able to publish Cobertura analysis report

  Scenario: Record Cobertura coverage report
    Given I have installed the "cobertura" plugin
    And a job
    When I configure the job
    And I copy resource "cobertura_plugin/coverage.xml" into workspace
    And I add "Publish Cobertura Coverage Report" post-build action
    And I set up "coverage.xml" as the Cobertura report
    And I save the job
    And I build the job
    Then the build should have "Coverage Report" action
    And the job should have "Coverage Report" action

  Scenario: View Cobertura coverage report
    Given I have installed the "cobertura" plugin
    And a job
    When I configure the job
    And I copy resource "cobertura_plugin/coverage.xml" into workspace
    And I add "Publish Cobertura Coverage Report" post-build action
    And I set up "coverage.xml" as the Cobertura report
    And I save the job
    And I build the job
    Then the build should succeed
    When I visit Cobertura report
    Then I should see the coverage of packages is 100%
    Then I should see the coverage of files is 50%
    Then I should see the coverage of classes is 31%
    Then I should see the coverage of methods is 23%
    Then I should see the coverage of lines is 16%
    Then I should see the coverage of conditionals is 10%

