Feature: Copy a job
  Copy a job and check if exists and has the same configuration as the original job

  Scenario: Copy a simple job
    When I create a job named "simple-job"
    And I copy the job named "simple-job-copy" from job named "simple-job"
    Then the page should say "simple-job-copy"
    And the job configuration should be equal to "simple-job" configuration
