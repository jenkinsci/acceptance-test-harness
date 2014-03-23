Feature: Use multi configuration job
  As a Jenkins user
  I want to configure and run multi configuration jobs

  *****************************************************
    BEING PORTED TO MatrixTest.java
  *****************************************************

  @since(1.515)
  @bug(7285)
  Scenario: Use Job parameters in combination filters
    Given a matrix job
    When I configure the job
    And I configure user axis "run" with values "yes maybe no"
    And I set combination filter to "run=='yes' || (run=='maybe' && condition=='true')"
    And I add a string parameter "condition"
    And I save the job
    And I build the job with parameter
        | condition | false |
    And I build the job with parameter
        | condition | true |
    Then combination "run=yes" should be built in build 1
    Then combination "run=yes" should be built in build 2
    Then combination "run=maybe" should not be built in build 1
    Then combination "run=maybe" should be built in build 2
    Then combination "run=no" should not be built in build 1
    Then combination "run=no" should not be built in build 2

  Scenario: Run configurations on with a given label
    Given a matrix job
    When I create dumb slave named "slave"
    And I add the label "label1" to the slave
    And I configure the job
    And I configure slaves axis with value "master"
    And I configure slaves axis with value "label1"
    And I save the job
    And I build the job
    Then the configuration "label=master" should be built on "master"
    And the configuration "label=label1" should be built on "slave"
