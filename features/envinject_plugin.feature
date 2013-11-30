Feature: Allow to inject build environment variables
  In order to be able to make the build more flexible
  As a Jenkins user
  I want to be able to have an isolated environment for my job.

  Scenario: Prepare environment for the build process via properties content
    Given I have installed the "envinject" plugin
    And a job
    When I prepare environment for the build by injecting variables
        """
            ENV_VAR_TEST=injected variable test
        """
    And I add a shell build step "echo $ENV_VAR_TEST"
    And I save the job
    And I build the job
    Then console output should match "^ENV_VAR_TEST=injected variable test$"
    Then console output should match "^injected variable test$"

  Scenario: Inject environment variables to the build process via properties content
    Given I have installed the "envinject" plugin
    And a job
    When I inject environment variables to the build
        """
            ENV_VAR_TEST=injected variable test
        """
    And I add a shell build step "echo $ENV_VAR_TEST"
    And I save the job
    And I build the job
    Then console output should match "^ENV_VAR_TEST=injected variable test$"
    Then console output should match "^injected variable test$"

  Scenario: Inject environment variables as a build step via properties content
    Given I have installed the "envinject" plugin
    And a job
    When I add build step injecting variables to the build
        """
            ENV_VAR_TEST=injected variable test
        """
    And I add a shell build step "echo $ENV_VAR_TEST"
    And I save the job
    And I build the job
    Then console output should match "^ENV_VAR_TEST=injected variable test$"
    Then console output should match "^injected variable test$"
