Feature: Cleanup job workspace
  As a Jenkins user
  I want to automatically cleanup my workspace

  Scenario: Do not clean up by default
    Given I have installed the "ws-cleanup" plugin
    And a simple job
    When I configure the job
    And I add a shell build step "touch artifact"
    And I save the job
    And I build the job
    Then there should be "artifact" in the workspace

  Scenario: Clean up after build
    Given I have installed the "ws-cleanup" plugin
    And a simple job
    When I configure the job
    And I add a shell build step "touch artifact"
    And I add "Delete workspace when build is done" post-build action
    And I save the job
    And I build the job
    Then there should not be "artifact" in the workspace

  Scenario: Clean up before build
    Given I have installed the "ws-cleanup" plugin
    And a simple job
    When I configure the job
    And I check the "hudson-plugins-ws_cleanup-PreBuildCleanup" checkbox
    # Creating directory that already exists would fail the build
    And I add a shell build step "mkdir artifact.d"
    And I save the job
    And I build 2 jobs
    Then the build should succeed
    And there should be "artifact.d" in the workspace

