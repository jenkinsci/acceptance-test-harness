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

  Scenario: Publish rest of parameterized tests
    Given a job
    When I configure the job
    And I copy resource "junit/parameterized" into workspace
    And I set Junit archiver path "parameterized/*.xml"
    And I save the job
    And I build the job
    Then the build should be unstable
    And I visit build action named "Test Result"
    And "JUnit.testScore[0]" error summary should match "expected:<42> but was:<0>"
    And "JUnit.testScore[1]" error summary should match "expected:<42> but was:<1>"
    And "JUnit.testScore[2]" error summary should match "expected:<42> but was:<2>"
    And "TestNG.testScore" error summary should match "expected:<42> but was:<0>"
    And "TestNG.testScore" error summary should match "expected:<42> but was:<1>"
    And "TestNG.testScore" error summary should match "expected:<42> but was:<2>"
