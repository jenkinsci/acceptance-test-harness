Feature:  Allow use of Git as a build SCM
  In order to be able to use of Git as a build SCM
  As a Jenkins user
  I want to poll and checkout source code from Git repository

  @realupdatecenter
  Scenario: Install the Git plugin
    When I install the "git" plugin from the update center
    And I create a job named "git-test"
    Then the job should be able to use the "Git" SCM

  @realupdatecenter
  Scenario: Simple checkout from Git repository
    When I install the "git" plugin from the update center
    And I create a job named "git-test"
    And I check out code from Git repository "git://github.com/jenkinsci/git-plugin.git"
    And I add a script build step to run "git remote -v"
    And I save the job
    And I build the job
    Then the build should succeed
    And I should see console output matching "origin git://github.com/jenkinsci/git-plugin.git"

  @realupdatecenter
  Scenario: Checkout branch from Git repository
    When I install the "git" plugin from the update center
    And I create a job named "git-test"
    And I check out code from Git repository "git://github.com/jenkinsci/git-plugin.git"
    And I setup branch specifier to "svn"
    And I add a script build step to run "if [ `git rev-parse origin/svn` = `git rev-parse HEAD` ]; then exit 0; fi; exit 1"
    And I save the job
    And I build the job
    Then the build should succeed

