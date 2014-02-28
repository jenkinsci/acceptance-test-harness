Feature: Monitor disk usage builds and jobs
  In order to be able to identify jobs consuming a lot of disk space and know how many disk space is consumed by builds
  As a Jenkins user
  I want to monitor disk usage of builds and jobs

  Scenario: Install Disk usage plugin
    When I install the "disk-usage" plugin from the update center
    Then I should be able to configure Disk usage globally
    And plugin page "disk-usage" should exist

  Scenario: Record disk usage
    Given I have installed the "disk-usage" plugin
    When I update disk usage
    Then the disk usage should be updated

  Scenario: Show disk usage graph on project page
    Given I have installed the "disk-usage" plugin
    And a simple job
    When I build the job
    And I enable disk usage graph on the project page
    And I update disk usage
    Then the project page should contain disk usage graph

  Scenario: Reflect disk changes in disk usage report
    Given I have installed the "disk-usage" plugin
    And a job
    When I add a shell build step in the job configuration
        """
            touch file
        """
    And I build the job
    And I enable disk usage graph on the project page
    And I update disk usage
    Then the job workspace should occupy some space
    When I wipe out job workspace
    And I update disk usage
    Then the job workspace should occupy no space
