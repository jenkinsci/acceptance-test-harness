Feature: Allow to inject build environment variables
  In order to be able to make the build more flexible
  As a Jenkins user
  I want to be able to have an isolated environment for my job.

  @realupdatecenter
  Scenario: Install EnvInject plugin
    When I install the "envinject" plugin from the update center
    And I create a job named "envinject-test"
    Then I should be able to prepare an environment for the build
    And I the job should be able to use the "Inject environment variables to the build process" build environment action
    And I the job should be able to use the "Inject passwords to the build as environment variables" build environment action
    And the job should be able to use the "Inject environment variables" buildstep

  @realupdatecenter
  Scenario: Prepare environment for the build build process via properties content
    When I install the "envinject" plugin from the update center
    And I create a job named "envinject-test"
    And I prepare environment for the build by injecting variables "ENV_VAR_TEST=injected variable test"
    And I add a shell build step "echo $ENV_VAR_TEST"
    And I save the job
    And I build the job
    Then I should see console output matching regexp "^ENV_VAR_TEST=injected variable test$"
    Then I should see console output matching regexp "^injected variable test$"

  @realupdatecenter
  Scenario: Inject environment variables to the build process via properties content
    When I install the "envinject" plugin from the update center
    And I create a job named "envinject-test"
    And I inject environment variables "ENV_VAR_TEST=injected variable test" to the build
    And I add a shell build step "echo $ENV_VAR_TEST"
    And I save the job
    And I build the job
    Then I should see console output matching regexp "^ENV_VAR_TEST=injected variable test$"
    Then I should see console output matching regexp "^injected variable test$"

  @realupdatecenter
  Scenario: Inject environment variables as a build step via properties content
    When I install the "envinject" plugin from the update center
    And I create a job named "envinject-test"
    And I add build step injecting variables "ENV_VAR_TEST=injected variable test" to the build
    And I add a shell build step "echo $ENV_VAR_TEST"
    And I save the job
    And I build the job
    Then I should see console output matching regexp "^ENV_VAR_TEST=injected variable test$"
    Then I should see console output matching regexp "^injected variable test$"
