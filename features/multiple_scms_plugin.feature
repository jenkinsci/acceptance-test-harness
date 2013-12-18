Feature: Test Multiple SCMs plugin

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
            test -d model-ant-project/.svn
            cd git-plugin/
            test -f pom.xml
        """
    And I save the job
    And I build the job
    Then the build should succeed
