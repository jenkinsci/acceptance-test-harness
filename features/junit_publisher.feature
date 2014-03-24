Feature: Test Junit test result publisher

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
