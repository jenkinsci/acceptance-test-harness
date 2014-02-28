Feature: Batch tasks
  Scenario: Run batch task manually
    Given I have installed the "batch-task" plugin
    And a job
    When I configure the job
    And I add batch task "manual"
    And I add batch task "useless"
    And I save the job
    And I build the job
    And I run "manual" batch task manually
    Then the batch task "manual" should run
    Then the batch task "useless" should not run

  Scenario: Trigger batch task
    Given I have installed the "batch-task" plugin
    And a job
    When I configure the job
    And I add batch task "runit"
    And I add batch task "dontrunit"
    And I configure batch trigger for "runit"
    And I save the job
    And I build the job
    Then the build should succeed
    And the batch task "runit" should run
    And the batch task "dontrunit" should not run

  Scenario: Trigger batch task on other job
    Given I have installed the "batch-task" plugin
    When I create a job named "target"
    And I configure the job
    And I add batch task "runit"
    And I add batch task "dontrunit"
    And I save the job
    And I build the job

    And I create a job named "trigger"
    And I configure the job
    And I configure "target" batch trigger for "runit"
    And I save the job
    And I build the job

    Then the build should succeed
    And "target" batch task "runit" should run
    And "target" batch task "dontrunit" should not run

  Scenario: Do not trigger for failed build
    Given I have installed the "batch-task" plugin
    And a job
    And I configure the job
    And I add batch task "dontrunit"
    And I add always fail build step
    And I configure batch trigger for "dontrunit"
    And I save the job
    And I build the job
    Then the batch task "dontrunit" should not run
