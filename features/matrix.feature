Feature: Use multi configuration job
  As a Jenkins user
  I want to configure and run multi configuration jobs

  *****************************************************
    BEING PORTED TO MatrixTest.java
  *****************************************************

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
