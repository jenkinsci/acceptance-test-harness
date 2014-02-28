Feature: Set the description for each build, based upon a RegEx test of the build log file
  In order to be able to see important build information on project and build page
  As a Jenkins user
  I want to be able to set build description based upon regular expression

  Scenario: Set build description based upon build log file
    Given I have installed the "description-setter" plugin
    And a job
    When I configure the job
    And I add a shell build step "echo '=== test ==='"
    And I add "Set build description" post-build action
    And I set up "===(.*)===" as the description setter reg-exp
    And I set up "Descrption setter test works!" as the description setter description
    And I save the job
    And I build the job
    Then the build should have description "Descrption setter test works!"
    Then the build should have description "Descrption setter test works!" in build history

