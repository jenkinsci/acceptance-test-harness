Feature: Tests for PMD plugin

  Scenario: Configure a job with PMD post-build steps
    Given I have installed the "pmd" plugin
    And a job
    When I configure the job
    And I add "Publish PMD analysis results" post-build action
    And I copy resource "pmd_plugin/pmd.xml" into workspace
    And I set path to the pmd result "pmd.xml"
    And I save the job
    And I build the job
    Then the build should succeed
    And build page should has pmd summary "0 warnings"

  Scenario: Configure a job with PMD post-build steps to run always
    Given I have installed the "pmd" plugin
    And a job
    When I configure the job
    And I add "Publish PMD analysis results" post-build action
    And I copy resource "pmd_plugin/pmd.xml" into workspace
    And I set path to the pmd result "pmd.xml"
    And I add always fail build step
    And I set publish always pdm
    And I save the job
    And I build the job
    Then the build should fail
    And build page should has pmd summary "0 warnings"

  Scenario: Configure a job with PMD post-build steps which display some warnings
    Given I have installed the "pmd" plugin
    And a job
    When I configure the job
    And I add "Publish PMD analysis results" post-build action
    And I copy resource "pmd_plugin/pmd-warnings.xml" into workspace
    And I set path to the pmd result "pmd-warnings.xml"
    And I save the job
    And I build the job
    Then the build should succeed
    And the build should have "PMD Warnings" action
    And build page should has pmd summary "9 warnings"

