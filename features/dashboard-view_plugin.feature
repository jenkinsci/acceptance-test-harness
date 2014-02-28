Feature: Add spport for dashboards
  In order to be able to create custom dashboards
  As a Jenkins user
  I want to install and configure dashboard-view plugin

  Scenario: Configure dashboard
    Given I have installed the "dashboard-view" plugin
    When I create a view with a type "Dashboard" and name "dashboard"
    And I configure dummy dashboard
    And I create job "job_in_view" in the view
    And I build "job_in_view" in view
    Then the build should succeed
    And the dashboard sould contain details of "job_in_view"
