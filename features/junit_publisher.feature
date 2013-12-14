Feature: Test Junit test result publisher

  Scenario: Publish test result which passed
    When I create a job named "javadoc-test"
    And I configure the job
    And I copy resource "junit/success" into workspace
    And I set Junit archiver path "success/*.xml"
    And I save the job
    And I build the job
    Then the build should succeed
    And I visit build action named "Test Result"
    Then the page should say "0 failures"

 Scenario: Publish test result which failed
    When I create a job named "javadoc-test"
    And I configure the job
    And I copy resource "junit/failure" into workspace
    And I set Junit archiver path "failure/*.xml"
    And I save the job
    And I build the job
    Then the build should be unstable
    And I visit build action named "Test Result"
    Then the page should say "1 failures"
