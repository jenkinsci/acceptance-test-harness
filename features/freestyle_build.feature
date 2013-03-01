Feature: Configure/build freestyle jobs
  In order to get some basic usage out of freestyle jobs
  As a user
  I want to configure and run a series of different freestyle-based jobs

  Scenario: Create a simple job
    When I create a job named "MAGICJOB"
    And I visit the home page
    Then the page should say "MAGICJOB"

  Scenario: Run a simple job
    Given a job
    When I configure the job
    And I add a script build step to run "ls"
    And I save the job
    And I build the job
    Then I should see console output matching "+ ls"

  Scenario: Disable a job
    Given a job
    When I configure the job
    And I check the "disable" checkbox
    And I save the job
    And I visit the job page
    Then the page should say "This project is currently disabled"

  Scenario: Enable concurrent builds
    Given a job
    When I configure the job
    And I enable concurrent builds
    And I add a script build step to run "sleep 20"
    And I save the job
    And I build 2 jobs
    Then the 2 jobs should run concurrently

  Scenario: Create a parameterized job
    Given a job
    When I configure the job
    And I add a string parameter "Foo"
    And I build the job
    Then I should be prompted to enter the "Foo" parameter

  Scenario: Disable a job
    Given a job
    When I disable the job
    Then it should be disabled
    And it should have an "Enable" button on the job page

  Scenario: Old build should be discarded
    Given a simple job
    When I configure the job
    And I set 2 builds to keep
    And I save the job
    And I build 4 jobs
    Then the job should not have build 1
    And  the job should not have build 2
    And  the job should have build 3
    And  the job should have build 4
    And  the job should not have build 5

  Scenario: Do not discard build kept forever
    Given a simple job
    When I configure the job
    And I set 1 build to keep
    And I save the job
    And I build the job
    And I lock the build
    And I build 2 jobs
    Then the job should have build 1
    And  the job should not have build 2
    And  the job should have build 3
    And  the job should not have build 4

  Scenario: Do not discard last successful build
    Given a simple job
    When I configure the job
    And I set 1 build to keep
    And I save the job
    And I build the job
    And I configure the job
    And I add always fail build step
    And I save the job
    And I build 2 jobs
    Then the job should have build 1
    And  the job should not have build 2
    And  the job should have build 3
    And  the job should not have build 4

  Scenario: Archive artifact and check content of archived artifact
    Given a job
    When I add a shell build step to run "echo 'archive test' > test.txt" in the job configuration
    And I add archive the artifacts "test.txt" in the job configuration
    And I build the job
    And the build completes
    Then the build should succeed
    And I should see console output matching "Archiving artifacts"
    And the artifact "test.txt" should be archived
    And the content of artifact "test.txt" should be "archive test"

  Scenario: Archive artifact and exclude another
    Given a job
    When I add a shell build step to run "echo 'archive include test' > test1.txt; echo 'archive exclude test' > test2.txt" in the job configuration
    And I add archive the artifacts "test1.txt" and exclude "test2.txt" in the job configuration
    And I build the job
    And the build completes
    Then the build should succeed
    And the artifact "test1.txt" should be archived
    And the artifact "test2.txt" should not be archived

  @realupdatecenter
  Scenario: Add Auto-Installed Java
    Given I add Java version "jdk-7u11-oth-JPR" with name "jdk_1.7.0" installed automatically to Jenkins config page
    And a job
    When I add a script build step to run "java -version"
    And I save the job
    And I build the job
    And the build completes
    Then I should see console output matching "Installing JDK jdk-7u11-oth-JPR"
    Then I should see console output matching "Downloading JDK from http://download.oracle.com"
    # Then I should see console output matching "java version "1.7.0_11""

# vim: tabstop=2 expandtab shiftwidth=2
