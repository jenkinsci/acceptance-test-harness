Feature: Sumarize job parameters
  In order to be able to check code style of my project
  As a Jenkins user
  I want to be able to publish Checkstyle report

  Scenario: Show freestyle job parameter summary
    Given I have installed the "job-parameter-summary" plugin
    And a job
    When I configure the job
    And I add a string parameter "MY_STRING_PARAM_0" defaulting to "MY_STRING_VAL_0"
    And I add a string parameter "MY_STRING_PARAM_1" defaulting to "MY_STRING_VAL_1"
    And I save the job
    Then summary should contain String Parameter "MY_STRING_PARAM_0" defaulting to "MY_STRING_VAL_0"
    And summary should contain String Parameter "MY_STRING_PARAM_1" defaulting to "MY_STRING_VAL_1"
