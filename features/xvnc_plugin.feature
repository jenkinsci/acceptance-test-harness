Feature: Adds Xvnc support
  In order to be able to run Xvnc based builds
  As a Jenkins user
  I want to able to setup Xvnc server

  Scenario: Install Xvnc plugin
    When I install the "xvnc" plugin from the update center
    And I create a job named "ant-test"
    Then the job should be able to use the "Invoke Ant" buildstep