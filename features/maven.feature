Feature: Adds Apache Maven support
  In order to be able to build Maven projects
  As a Jenkins user
  I want to install and configure Maven and build Maven based project

  @realupdatecenter
  Scenario: Use Auto-Installed Maven from FreeStyle job
    Given I add Maven version "3.0.4" with name "maven_3.0.4" installed automatically to Jenkins config page
    And a job
    When I configure the job
    And I add a top-level maven target "archetype:generate -DarchetypeGroupId=org.apache.maven.archetypes -DgroupId=com.mycompany.app -DartifactId=my-app -Dversion=1.0 -B -X" for maven "maven_3.0.4"
    And I save the job
    And I build the job
    Then I should see console output matching "Unpacking http://archive.apache.org/dist/maven/binaries/apache-maven-3.0.4-bin.zip"
    Then I should see console output matching "Apache Maven 3.0.4"
    And the build should succeed

  Scenario: Use locally installed Maven from FreeStyle job
    Given fake Maven installation at "/tmp/fake-maven"
    And I add Maven with name "local_maven_3.0.4" and Maven home "/tmp/fake-maven" to Jenkins config page
    And a job
    When I configure the job
    And I add a top-level maven target "archetype:generate -DarchetypeGroupId=org.apache.maven.archetypes -DgroupId=com.mycompany.app -DartifactId=my-app -Dversion=1.0 -B -X" for maven "local_maven_3.0.4"
    And I save the job
    And I build the job
    Then I should see console output matching "fake maven at /tmp/fake-maven/bin/mvn"
    And the build should succeed

  Scenario: Use local Maven repository from FreeStyle job
    Given a Maven
    And a job
    When I configure the job
    And I add a top-level maven target "archetype:generate -DarchetypeGroupId=org.apache.maven.archetypes -DgroupId=com.mycompany.app -DartifactId=my-app -Dversion=1.0 -B"
    And I use local Maven repository
    And I save the job
    And I build the job
    Then I should see console output matching regexp "-Dmaven.repo.local=([^\n]*)/.repository"
    And the build should succeed

  @realupdatecenter
  Scenario: Build multimodule Maven project
    Given I have default Maven configured
    And a Maven job
    When I configure the job
    And I copy resource "maven/repositories/multimodule/*" into workspace via shell command
    And I add a top-level maven target "package"
    And I save the job
    And I build the job
    Then the build should succeed
    And the job should have "Modules" action
    And I should see console output matching "[INFO] Building root 1.0"
    And I should see console output matching "[INFO] Building module_a 2.0"
    And I should see console output matching "[INFO] Building module_b 3.0"

  @realupdatecenter
  Scenario: Set maven options
    Given I have default Maven configured
    And a Maven job
    When I configure the job
    And I copy resource "maven/repositories/multimodule/*" into workspace via shell command
    And I add a top-level maven target "package"
    And I set maven options "--quiet"
    And I save the job
    And I build the job
    Then I should not see console output matching "[INFO]"
    And  I should not see console output matching "[WARNING]"

  @bug(17713)
  @realupdatecenter
  Scenario: Display job modules for Maven project
    And I have default Maven configured
    And a Maven job
    When I configure the job
    And I copy resource "maven/repositories/multimodule/*" into workspace via shell command
    And I add a top-level maven target "package"
    And I save the job
    And I build the job
    Then the job should have module named "gid$root"
    Then the job should have module named "gid$module_a"
    Then the job should have module named "gid$module_b"
