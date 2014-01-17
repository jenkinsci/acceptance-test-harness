Feature: Tests for SCP plugin

  @native(docker)
  Scenario: Configure a job with SCP publishing
    Given I have installed the "scp" plugin
    And a docker fixture "sshd"
    And a job
    When I configure docker fixture as SCP site
    And I configure the job
    And I copy resource "pmd_plugin/pmd.xml" into workspace
    And I publish "pmd.xml" with SCP plugin
    And I save the job
    And I build the job
    Then the build should succeed
    And SCP plugin should have published "pmd.xml" on docker fixture
