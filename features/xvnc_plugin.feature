Feature: Adds Xvnc support
  In order to be able to run Xvnc based builds
  As a Jenkins user
  I want to able to setup Xvnc server

  @realupdatecenter
  Scenario: Install Xvnc plugin
    When I install the "xvnc" plugin from the update center
    Then I should be able to configure Xvnc globally
