Feature: Adds Warnings collection support
  In order to be able to collect and analyze warnings
  As a Jenkins user
  I want to install and configure Warnings plugin

  Scenario: Detect no errors in console log and workspace when there are none
    Given I have installed the "warnings" plugin
    And a job
    When I configure the job
    And I add "Scan for compiler warnings" post-build action
    And I add console parser for "Maven"
    And I add workspace parser for "Java Compiler (javac)" applied at "**/*"
    And I save the job
    And I build the job
    Then build should have 0 "Java" warnings
    Then build should have 0 "Maven" warnings

  Scenario: Detect errors in console log
    Given I have installed the "warnings" plugin
    And a job
    When I configure the job
    And I add "Scan for compiler warnings" post-build action
    And I add console parser for "Maven"
    And I add a shell build step "mvn clean install || true"
    And I save the job
    And I build the job
    Then build should have 1 "Maven" warning

  Scenario: Detect errors in workspace
    Given I have installed the "warnings" plugin
    And a job
    When I configure the job
    And I add "Scan for compiler warnings" post-build action
    And I add workspace parser for "Java Compiler (javac)" applied at "**/*"
    And I add a shell build step
        """
            echo '@Deprecated class a {} class b extends a {}' > a.java
            javac -Xlint a.java 2> out.log || true
        """
    And I save the job
    And I build the job
    Then build should have 1 "Java" warning

  Scenario: Do not detect errors in ignored parts of the workspace
    Given I have installed the "warnings" plugin
    And a job
    When I configure the job
    And I add "Scan for compiler warnings" post-build action
    And I add workspace parser for "Maven" applied at "no_errors_here.log"
    And I add a shell build step "mvn clean install > errors.log || true"
    And I save the job
    And I build the job
    Then build should have 0 "Maven" warning
