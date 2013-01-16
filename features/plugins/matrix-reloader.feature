Feature: Test Matrix-realoader plugin
 
  @realupdatecenter
  Scenario: Instal Matrix-realoader plugin
    When I install the "matrix-reloaded" plugin from the update center
    And I create a matrix job   
    And I run the job
    Then the job should see "Matrix Reloaded" action on the build page

  @realupdatecenter
  Scenario: Build Matrix configuration
    Given I have installed the "matrix-reloaded" plugin
    When I create a matrix job
    And I configure user axis "AAA" with values "111 222"
    And I configure user axis "BBB" with values "333 444"
    And I save the job
    And I run the job
    And I visit "Matrix Reloaded" action on build page
    Then I should see matrix configuration "AAA=111,BBB=333"
    And I should see matrix configuration "AAA=111,BBB=444"
    And I should see matrix configuration "AAA=222,BBB=333"
    And I should see matrix configuration "AAA=222,BBB=444"

  @realupdatecenter
  Scenario: Run Matrix configuration
    Given I have installed the "matrix-reloaded" plugin
    When I create a matrix job 
    And I configure user axis "AAA" with values "111 222"
    And I configure user axis "BBB" with values "333 444"
    And I save the job
    And I run the job
    And I visit "Matrix Reloaded" action on build page
    And I select matrix configuration "AAA=111,BBB=333"
    And I rebuild matrix job
    Then combination "AAA=111,BBB=333" should be built
    And combination "AAA=111,BBB=444" should not be built
    And combination "AAA=222,BBB=333" should not be built
    And combination "AAA=222,BBB=444" should not be built
