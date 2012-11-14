Feature: Subversion support
  As a user
  I want to be able to check out source code from Subversion

  @realupdatecenter
  Scenario: Run basic Subversion build
    Given I have installed the "subversion" plugin
    And a job
    When I check out code from Subversion repository "https://svn.jenkins-ci.org/trunk/jenkins/test-projects/model-ant-project/"
    And I add a script build step to run "test -d .svn"
    And I save the job
    And I build 1 jobs
    Then the build should succeed


