Feature: Test JobConfigHistory plugin
 
  @realupdatecenter
  Scenario: Install JobConfigHistory plugin
    When I install the "jobConfigHistory" plugin from the update center
    And I create a job named "config-history-test"
    Then I should see "Job Config History" action on the job page

 @realupdatecenter
  Scenario: Save job config history
    Given I have installed the "jobConfigHistory" plugin
    And a simple job
    When I configure the job
    And I add a script build step to run "ls"
    And I save the job
    And I visit "Job Config History" action on the job page
    Then jobConfigHistory page should show difference

 @realupdatecenter
  Scenario: Show difference in config history
    Given I have installed the "jobConfigHistory" plugin
    And a simple job
    When I configure the job
    And I add a script build step to run "ls"
    And I save the job
    And I configure the job
    And I change a script build step to run "ls -ls"
    And I save the job
    And I visit "Job Config History" action on the job page
    And I dispaly difference
    Then configuration should have "<command>ls -ls</command>" instead of "<command>ls</command>"
