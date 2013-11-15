Feature: Allow reporting for FindBugs analysis
  In order to be able to see possible problems in the project code easily
  As a Jenkins user
  I want to be able to show reports from FindBugs analysis

  Scenario: Record FindBugs analysis
    Given I have installed the "findbugs" plugin
    And a job
    When I configure the job
    And I copy resource "findbugs_plugin/findbugsXml.xml" into workspace
    And I add "Publish FindBugs analysis results" post-build action
    And I set up "findbugsXml.xml" as the FindBugs results
    And I save the job
    And I build the job
    Then the build should have "FindBugs Warnings" action
    And the job should have "FindBugs Warnings" action
