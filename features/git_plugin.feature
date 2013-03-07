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
    And I add a shell build step "git remote -v"
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
    And I add a shell build step "if [ `git rev-parse origin/svn` = `git rev-parse HEAD` ]; then exit 0; fi; exit 1"
    And I save the job
    And I build the job
    Then the build should succeed

  @realupdatecenter
  Scenario: Checkout branch from Git repository
    When I install the "git" plugin from the update center
    And I create a job named "git-test"
    And I check out code from Git repository "git://github.com/jenkinsci/git-plugin.git"
    And I navigate to Git Advanced section
    And I setup local branch to "selenium_test_branch"
    And I add a shell build step "if [ `git rev-parse selenium_test_branch` = `git rev-parse HEAD` ]; then exit 0; fi; exit 1"
    And I save the job
    And I build the job
    Then the build should succeed

  @realupdatecenter
  Scenario: Simple checkout from Git repository
    When I install the "git" plugin from the update center
    And I create a job named "git-test"
    And I check out code from Git repository "git://github.com/jenkinsci/git-plugin.git"
    And I navigate to Git Advanced section
    And I setup local Git repo dir to "selenium_test_dir"
    And I add a shell build step "if [ ! -d selenium_test_dir ]; then exit 1; fi; cd selenium_test_dir; git remote -v"
    And I save the job
    And I build the job
    Then the build should succeed
    And I should see console output matching "origin git://github.com/jenkinsci/git-plugin.git"

  @realupdatecenter
  Scenario: Simple checkout from Git repository
    When I install the "git" plugin from the update center
    And I create a job named "git-test"
    And I check out code from Git repository "git://github.com/jenkinsci/git-plugin.git"
    And I navigate to Git Remote config Advanced section
    And I setup Git repo name to "selenium_test_repo"
    And I add a shell build step "git remote -v"
    And I save the job
    And I build the job
    Then the build should succeed
    And I should see console output matching "selenium_test_repo git://github.com/jenkinsci/git-plugin.git"
