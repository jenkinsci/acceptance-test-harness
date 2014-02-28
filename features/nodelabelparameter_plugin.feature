Feature: Use node name and label as parameter
  In order to control where a build should run at the time it is triggered
  As a Jenkins user
  I want to specify the name of the slave or the label as a build parameter

  Scenario: Build on a particular slave
    Given I have installed the "nodelabelparameter" plugin
    And a job
    And a slave named "slave42"
    When I configure the job
    And I add node parameter "slavename"
    And I save the job
    And I build the job with parameter
        | slavename | slave42 |
    Then the build should run on "slave42"

  Scenario: Run on label
    Given I have installed the "nodelabelparameter" plugin
    And a job
    And a slave named "slave42"
    And a slave named "slave43"
    When I configure the job
    And I add label parameter "slavelabel"
    And I save the job
    And I build the job with parameter
        | slavelabel | slave42 |
    Then the build should run on "slave42"
    And I build the job with parameter
        | slavelabel | !slave42 && !slave43 |
    Then the build should run on "master"

  Scenario: Run on several slaves
    Given I have installed the "nodelabelparameter" plugin
    And a job
    And a slave named "slave42"
    When I configure the job
    And I add node parameter "slavename"
    And I allow multiple nodes
    And I enable concurrent builds
    And I save the job
    And I build the job with parameter
        | slavename | slave42, master |
    Then the job should have 2 builds
    And  the job should be built on "master"
    And  the job should be built on "slave42"
