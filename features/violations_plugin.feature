Feature: Add violations support
  In order to be able to detect violation
  As a Jenkins user
  I want to configure, collect and visualize violations

  Scenario: Report violations in FreeStyle project
    Given I have installed the "violations" plugin
    And a job
    When I configure the job
    And I copy resource "violations_plugin/*" into workspace
    And I configure violations reporting
    And I set "fxcop" reporter applied at "fxcop/*"
    And I save the job
    And I build the job
    Then the build should have "Violations" action
    And the job should have "Violations" action
    And there should be 2 "fxcop" violations in 2 files

  Scenario: Report violations in Maven project
    Given I have installed the "violations" plugin
    And I have default Maven configured
    And a Maven job
    When I configure the job
    And I copy resource "violations_plugin/*" into workspace
    And I configure violations reporting
    And I set "fxcop" reporter applied at "fxcop/*"
    And I save the job
    And I build the job
    Then the build should have "Violations" action
    And the job should have "Violations" action
    And there should be 2 "fxcop" violations in 2 files for module "gid$example"
