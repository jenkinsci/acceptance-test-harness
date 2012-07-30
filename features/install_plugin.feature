Feature: Install plugins from the update center
  In order to make Jenkins more useful for non-default uses

  As a Jenkins user

  I should be able to browse a number of plugins and install them directly from
  within Jenkins itself

  @wip @realupdatecenter
  Scenario: Install the Git plugin
    Given a bare Jenkins instance
    When I install the "git" plugin from the update center
    And I create a job named "git-test"
    Then the job should be able to use the Git SCM


