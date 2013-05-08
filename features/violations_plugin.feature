Feature: Add violations support
  In order to be able to detect violation
  As a Jenkins user

  @realupdatecenter
  Scenario: Report violations in FreeStyle project
    Given I have installed the "violations" plugin
    And a job
    When I configure the job
    And I copy resource "violations_plugin/*" into workspace via shell command
    And I configure violations reporting
    And I set "fxcop" reporter applied at "fxcop/*"
    And I save the job
    And I build the job
    Then the build should succeed
    Then the job should have "Violations" action
    And the build should have "Violations" action
    And there should be 2 "fxcop" violations in 2 files
