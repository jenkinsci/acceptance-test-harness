Feature: Adds Apache Maven support
  In order to be able to build Maven projects
  As a Jenkins user
  I want to install and configure Maven and build Maven based project

  Scenario: Use Auto-Installed Maven from FreeStyle job
    Given I have Maven "3.0.4" auto-installation named "maven_3.0.4" configured
    And a job
    When I configure the job
    And I add a top-level maven target for maven "maven_3.0.4"
        """
            archetype:generate -DarchetypeGroupId=org.apache.maven.archetypes -DgroupId=com.mycompany.app -DartifactId=my-app -Dversion=1.0 -B -X
        """
    And I save the job
    And I build the job
    Then the build should succeed
    And console output should contain "Apache Maven 3.0.4"
    And console output should contain
        """
            Unpacking http://archive.apache.org/dist/maven/binaries/apache-maven-3.0.4-bin.zip
        """


  Scenario: Use Auto-Installed Maven2 from FreeStyle job
    Given I have Maven "2.2.1" auto-installation named "maven_2.2.1" configured
    And a job
    When I configure the job
    And I add a top-level maven target for maven "maven_2.2.1"
        """
            archetype:generate -DarchetypeGroupId=org.apache.maven.archetypes -DgroupId=com.mycompany.app -DartifactId=my-app -Dversion=1.0 -B -X
        """
    And I save the job
    And I build the job
    Then the build should succeed
    And console output should contain "Apache Maven 2.2.1"
    And console output should contain
        """
            Unpacking http://archive.apache.org/dist/maven/binaries/apache-maven-2.2.1-bin.zip
        """


  Scenario: Use locally installed Maven from FreeStyle job
    Given fake Maven installation at "/tmp/fake-maven"
    And I have Maven "local_maven_3.0.4" installed in "/tmp/fake-maven" configured
    And a job
    When I configure the job
    And I add a top-level maven target for maven "local_maven_3.0.4"
        """
           archetype:generate -DarchetypeGroupId=org.apache.maven.archetypes -DgroupId=com.mycompany.app -DartifactId=my-app -Dversion=1.0 -B -X
        """
    And I save the job
    And I build the job
    Then console output should contain "fake maven at /tmp/fake-maven/bin/mvn"
    And the build should succeed

  Scenario: Use local Maven repository from FreeStyle job
    Given a Maven
    And a job
    When I configure the job
    And I add a top-level maven target
        """
           archetype:generate -DarchetypeGroupId=org.apache.maven.archetypes -DgroupId=com.mycompany.app -DartifactId=my-app -Dversion=1.0 -B
        """
    And I use local Maven repository
    And I save the job
    And I build the job
    Then console output should match "-Dmaven.repo.local=([^\n]*)/.repository"
    And the build should succeed

  Scenario: Build multimodule Maven project
    Given I have default Maven configured
    And a Maven job
    When I configure the job
    And I copy resource "maven/repositories/multimodule/*" into workspace
    And I add a top-level maven target "package"
    And I save the job
    And I build the job
    Then the build should succeed
    And the job should have "Modules" action
    And console output should contain "Building root 1.0"
    And console output should contain "Building module_a 2.0"
    And console output should contain "Building module_b 3.0"

  Scenario: Set maven options
    Given I have default Maven configured
    And a Maven job
    When I configure the job
    And I copy resource "maven/repositories/multimodule/*" into workspace
    And I add a top-level maven target "package"
    And I set maven options "--quiet"
    And I save the job
    And I build the job
    Then console output should match "[INFO]"
    And  console output should match "[WARNING]"

  Scenario: Set global maven options
    Given I have default Maven configured
    And a Maven job
    When I configure the job
    And I copy resource "maven/repositories/multimodule/*" into workspace
    And I add a top-level maven target "package"
    And I save the job
    And I set global MAVEN_OPTS "-verbose"
    And I build the job
    Then console output should match "[Loaded java\.lang\.Objects from .*jar]"

  @bug(17713)
  Scenario: Display job modules for Maven project
    Given I have default Maven configured
    And a Maven job
    When I configure the job
    And I copy resource "maven/repositories/multimodule/*" into workspace
    And I add a top-level maven target "package"
    And I save the job
    And I build the job
    Then the job should have module named "gid:root"
    Then the job should have module named "gid:module_a"
    Then the job should have module named "gid:module_b"

  @bug(10539)
  @since(1.527)
  Scenario: Preserve backslash in property
    Given I have default Maven configured
    And a job
    When I configure the job
    And I add a string parameter "CMD" defaulting to "C:\System"
    And I add a string parameter "PROPERTY" defaulting to "C:\Windows"
    And I copy resource "maven/repositories/multimodule/*" into workspace
    And I add a top-level maven target "validate -Dcmdline.property=$CMD"
    And I set maven properties
       """
       property.property=$PROPERTY
       """
    And I save the job
    And I build the job
    Then console output should contain "cmdline.property=C:\System"
    And  console output should contain "property.property=C:\Windows"
