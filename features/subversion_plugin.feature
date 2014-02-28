Feature: Subversion support
  As a user
  I want to be able to check out source code from Subversion

  Scenario: Run basic Subversion build
    Given I have installed the "subversion" plugin
    And a job
    When I check out code from Subversion repository "https://svn.jenkins-ci.org/trunk/jenkins/test-projects/model-ant-project/"
    And I add a shell build step "test -d .svn"
    And I save the job
    And I build the job
    Then the build should succeed
    And console output should contain "test -d .svn"

  Scenario: Check out specified Subversion revision
    Given I have installed the "subversion" plugin
    And a job
    When I check out code from Subversion repository "https://svn.jenkins-ci.org/trunk/jenkins/test-projects/model-ant-project@40156"
    And I save the job
    And I build the job
    Then the build should succeed
    And console output should contain "At revision 40156"

  Scenario: Always check out fresh copy
    Given I have installed the "subversion" plugin
    And a job
    When I check out code from Subversion repository "https://svn.jenkins-ci.org/trunk/jenkins/test-projects/model-ant-project"
    And I select "Always check out a fresh copy" as a "Check-out Strategy"
    And I save the job
    And I build 2 jobs
    Then the build should succeed
    And console output should contain "Checking out https://svn.jenkins-ci.org/trunk/jenkins/test-projects/model-ant-project"
