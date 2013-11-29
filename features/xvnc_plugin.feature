Feature: Adds Xvnc support
  In order to be able to run Xvnc based builds
  As a Jenkins user
  I want to able to setup Xvnc server

  @native(vncserver)
  Scenario: Run xvnc during build
    Given I have installed the "xvnc" plugin
    And a job
    When I configure the job
    And I let xvnc run during the build
    And I save the job
    And I build the job
    Then the build should succeed
    And xvnc run during the build

  @native(vncserver,import)
  Scenario: Run xvnc during build taking screenshot at the end
    Given I have installed the "xvnc" plugin
    And a job
    When I configure the job
    And I let xvnc run during the build taking screanshot at the end
    And I save the job
    And I build the job
    Then the build should succeed
    And xvnc run during the build
    And took a screanshot

  @native(vncserver)
  Scenario: Run xvnc on specific display number
    Given I have installed the "xvnc" plugin
    And a job
    When I set xvnc display number to 42
    And I configure the job
    And I let xvnc run during the build
    And I save the job
    And I build the job
    Then the build should succeed
    And xvnc run during the build
    And used display number 42
