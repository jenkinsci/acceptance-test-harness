Feature: Publish HTML directories
  As a Jenkins user
  I want to publishe various html reports

  Scenario: Publish whole directory
    Given I have installed the "htmlpublisher" plugin
    And a simple job
    When I configure the job
    And I copy resource "htmlpublisher_plugin/*" into workspace
    And I configure "." directory to be published as "My report"
    And I set index file to "home.html"
    And I save the job
    And I build the job
    Then the build should succeed
    And the html report "My report" should be correclty published
