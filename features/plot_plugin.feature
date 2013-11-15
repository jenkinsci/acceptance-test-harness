Feature: Adds Plotting support
  In order to be able to visualize build metrics
  As a Jenkins user
  I want to configure and generate various plots

  Scenario: Generate simple plot
    Given I have installed the "plot" plugin
    And a job
    When I configure the job
    And I copy resource "plot_plugin/plot.csv" into workspace
    And I add plot "My plot" in group "My group"
    And I configure csv data source "plot.csv"
    And I save the job
    And I build the job
    Then the build should succeed
    And there should be a plot called "My plot" in group "My group"

  @bug(18585)
  @bug(18674)
  Scenario: Post-build rendering should work
    Given I have installed the "plot" plugin
    And a job
    When I configure the job
    And I add plot "Some plot" in group "Plots"
    And I save the job
    And I configure the job
    And I configure csv data source "plot.csv"
    And I save the job
    And I build the job
    Then the build should succeed
    And there should be a plot called "Some plot" in group "Plots"
