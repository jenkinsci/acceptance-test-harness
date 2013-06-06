Feature: Sumarize job parameters
  In order to be able to check code style of my project
  As a Jenkins user
  I want to be able to publish Checkstyle report

  @realupdatecenter
  Scenario: Show freestyle job parameter summary
    Given I have installed the "job-parameter-summary" plugin
    And a job
    When I add a string parameter "MY_STRING_PARAM" defaulting to "MY_STRING_VAL"
    And I visit the job page
    Then summary should contain String Parameter "MY_STRING_PARAM" defaulting to "MY_STRING_VAL"
