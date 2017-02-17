Feature: Configure/build freestyle jobs
  In order to get some basic usage out of freestyle jobs
  As a user
  I want to configure and run a series of different freestyle-based jobs

  **********************************************************************************
     THIS TEST IS ALREADY PORTED OVER TO JAVA AS-IS. NO NEED TO PORT TO JUNIT
  **********************************************************************************


  Scenario: Do not discard last successful build
    Given a simple job
    When I configure the job
    And I set 1 build to keep
    And I save the job
    And I build the job
    And I configure the job
    And I add always fail build step
    And I save the job
    And I build 2 jobs sequentially
    Then the job should have build 1
    And  the job should not have build 2
    And  the job should have build 3
    And  the job should not have build 4

  @bug(21457) @bug(20772) @bug(21478) @wip
  Scenario: Show error message after apply
    Given a job
    When I configure the job
    And I schedule job to run periodically at "not_a_time"
    And I click the "Apply" button
    Then the error description should contain
        """
        Invalid input: "not_a_time"
        """
    And I close the error dialog
    And I schedule job to run periodically at "not_a_time_either"
    And I click the "Apply" button
    Then the error description should contain
        """
        Invalid input: "not_a_time_either"
        """
