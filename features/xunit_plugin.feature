Feature: Add xUnit support

  Scenario: Publish xUnit results
    Given I have installed the "xunit" plugin
    And a job
    When I configure the job
    And I copy resource "junit/failure" into workspace
    And I publish "JUnit" report from "failure/TEST*.xml"
    And I save the job
    And I build the job
    Then the build should succeed
    And I visit build action named "Test Result"
    Then the page should say "1 failures"
