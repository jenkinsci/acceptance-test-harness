Feature: Test Multiple SCMs plugin
  
@realupdatecenter
  Scenario: Install the Multiple SCMs plugin
    When I install the "multiple-scms" plugin from the update center
    And I create a job named "multiple-scms-test"
    Then the job should be able to use the "Multiple SCMs" SCM

  @realupdatecenter
  Scenario: Provide multiple SCMs
    Given I have installed the "subversion" plugin
    And I have installed the "git" plugin
    And I have installed the "multiple-scms" plugin
    And a job
    When I configure the job
    And I choose Multiple SCMs SCM
    And I add a new SCM
    Then I should see "Git" SCM option
    And I should see "Subversion" SCM option

  @realupdatecenter
  Scenario: Checkout from multiple SCMs
    Given I have installed the "subversion" plugin
    And I have installed the "git" plugin
    And I have installed the "multiple-scms" plugin
    And a job
    When I configure the job
    And I choose Multiple SCMs SCM
    And I add Git scm url "git://github.com/jenkinsci/git-plugin.git" and directory name "git-plugin"
    And I add Subversion scm url "https://svn.jenkins-ci.org/trunk/jenkins/test-projects/model-ant-project/" and directory name "model-ant-project"
    And I add a shell build step
        """
            cd model-ant-project
            test -d .svn
            cd ..
            cd git-plugin
            git remote -v
        """
    And I save the job
    And I build the job
    Then the build should succeed
    And I should see console output matching "test -d .svn"
    And I should see console output matching "origin git://github.com/jenkinsci/git-plugin.git"  
 
