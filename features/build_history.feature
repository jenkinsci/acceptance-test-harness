Feature: Display build history
  As a Jenkins user or administrator
  I should be able to view the build history both globally or per-job
  So that I can identify build trends, times, etc.

  Scenario: Viewing global build history
    Given a simple job
    When I build the job
    Then the global build history should show the build
