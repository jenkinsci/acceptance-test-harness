Feature: PostBuildScript support
  As a Jenkins user
  I want to attach custom post-build steps

  Background:
    Given I have installed the "postbuildscript" plugin
    And a job

  Scenario: Execute shell post-build script
    When I add marker post-build script
    And I build the job
    Then the post-build script should have been executed

  Scenario: Do not execute fail post-build script for jobs that succeeded
    When I add marker post-build script
    And I allow the script to run only for builds that failed
    And I build the job
    Then the post-build script should not have been executed

  Scenario: Do not execute success post-build script for jobs that failed
    When I add marker post-build script
    And I allow the script to run only for builds that succeeded
    And I add always fail build step in the job configuration
    And I build the job
    Then the post-build script should not have been executed
