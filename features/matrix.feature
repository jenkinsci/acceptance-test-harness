Feature: Use multi configuration job
  As a Jenkins user
  I want to configure and run multi configuration jobs

  Scenario: Use Job parameters in combination filters
    Given a matrix job
    When I configure the job
    And I configure user axis "run" with values "yes maybe no"
    And I set combination filter to "run=='yes' || (run=='maybe' && condition=='true')"
    And I add a string parameter "condition"
    And I save the job
    And I build the job
    And I set "condition" parameter to "false"
    And I click the "Build" button
    And I build the job
    And I set "condition" parameter to "true"
    And I click the "Build" button
    Then combination "run=yes" should be built in build 1
    Then combination "run=yes" should be built in build 2
    Then combination "run=maybe" should not be built in build 1
    Then combination "run=maybe" should be built in build 2
    Then combination "run=no" should not be built in build 1
    Then combination "run=no" should not be built in build 2
