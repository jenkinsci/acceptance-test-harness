Feature: Adds Apache Maven support
  In order to be able to build Maven projects
  As a Jenkins user
  I want to install and configure Maven and build Maven based project

  @realupdatecenter
  Scenario: Add and use Auto-Installed Maven
    Given I add Maven version "3.0.4" with name "maven_3.0.4" installed automatically to Jenkins config page
    And a job
    When I configure the job
    And I add a top-level maven target "archetype:generate -DarchetypeGroupId=org.apache.maven.archetypes -DgroupId=com.mycompany.app -DartifactId=my-app -Dversion=1.0 -B -X" for maven "maven_3.0.4"
    And I save the job
    And I build the job
    And the build completes	
    Then I should see console output matching "Unpacking http://archive.apache.org/dist/maven/binaries/apache-maven-3.0.4-bin.zip"
    Then I should see console output matching "Apache Maven 3.0.4"
    And the build should succeed

