Feature: Test JobConfigHistory plugin

  Scenario: Save job config history
    Given I have installed the "jobConfigHistory" plugin
    And a simple job
    When I configure the job
    And I add a shell build step "ls"
    And I save the job
    And I visit job action named "Job Config History"
    Then jobConfigHistory page should show difference

  Scenario: Show difference in config history
    Given I have installed the "jobConfigHistory" plugin
    And a simple job
    When I configure the job
    And I add a shell build step "ls"
    And I save the job
    And I configure the job
    And I change a shell build step to "ls -ls"
    And I save the job
    And I visit job action named "Job Config History"
    And I dispaly difference
    Then configuration should have "<command>ls -ls</command>" instead of "<command>ls</command>"
