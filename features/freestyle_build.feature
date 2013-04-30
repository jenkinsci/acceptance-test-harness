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
    And I add a shell build step "ls"
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
    And I add a shell build step "sleep 20"
    And I save the job
    And I build 2 jobs
    Then the 2 jobs should run concurrently

  Scenario: Create a parameterized job
    Given a job
    When I configure the job
    And I add a string parameter "Foo"
    And I save the job
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
    When I add a shell build step "echo 'archive test' > test.txt" in the job configuration
    And I set artifact "test.txt" to archive in the job configuration
    And I build the job
    Then the build should succeed
    And I should see console output matching "Archiving artifacts"
    And the artifact "test.txt" should be archived
    And the content of artifact "test.txt" should be "archive test"

  Scenario: Archive artifact and exclude another
    Given a job
    When I add a shell build step in the job configuration
        """
            echo 'archive include test' > test1.txt
            echo 'archive exclude test' > test2.txt
        """
    And I set artifact "test1.txt" to archive and exclude "test2.txt" in the job configuration
    And I build the job
    Then the build should succeed
    And the artifact "test1.txt" should be archived
    And the artifact "test2.txt" should not be archived

  Scenario: Archive artifact and keep only the last successful
    Given a job
    When I add a shell build step "echo 'archive test' > test.txt" in the job configuration
    And I set artifact "test.txt" to archive in the job configuration
    And I want to keep only the latest successful artifacts
    And I build 3 jobs
    Then the build #1 should not has archived "test.txt" artifact
    And the build #2 should has archived "test.txt" artifact
    And the build #3 should has archived "test.txt" artifact

  @realupdatecenter
  Scenario: Add Auto-Installed Java
    Given I add Java version "jdk-7u11-oth-JPR" with name "jdk_1.7.0" installed automatically to Jenkins config page
    And a job
    When I add a shell build step "java -version"
    And I save the job
    And I build the job
    Then I should see console output matching "Installing JDK jdk-7u11-oth-JPR"
    Then I should see console output matching "Downloading JDK from http://download.oracle.com"
    # Then I should see console output matching "java version "1.7.0_11""

  Scenario: Schedule build periodically
    Given a job
    When I configure the job
    And I schedule job to run periodically at "* * * * *"
    And I save the job
    And I wait for 70 seconds
    Then the job should have build 1
    # number 2 might exist
    And  the job should not have build 3

  # JENKINS-16630
  @since(1.504)
  Scenario: Format zero-sized artifact size properly
    Given a job
    When I configure the job
    And I add a shell build step "touch empty.file"
    And I set artifact "empty.file" to archive
    And I save the job
    And I build the job
    Then the size of artifact "empty.file" should be "0 B"

  Scenario: Use custom workspace
    Given a job
    When I configure the job
    And I use "custom_workspace" as custom workspace
    And I save the job
    And I build the job
    Then I should see console output matching regexp "^Building in workspace (.*)custom_workspace$"

# vim: tabstop=2 expandtab shiftwidth=2
