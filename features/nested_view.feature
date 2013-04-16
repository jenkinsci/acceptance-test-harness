Feature: Test Nested view plugin
 
  @realupdatecenter
  Scenario: Install Nested view plugin
    When I install the "nested-view" plugin from the update center   
    Then I should see "Nested View" choice when I creating a new view

  @realupdatecenter
  Scenario: Create Nested view
    Given I have installed the "nested-view" plugin
    And a simple job
    When I create a view with a type "Nested View" and name "Nested"
    Then I should see the view on the main page

  @realupdatecenter
  Scenario: Add subviews to a Nested view
    Given I have installed the "nested-view" plugin
    And a simple job
    When I create a view with a type "Nested View" and name "Nested"
    And I create a subview of the view with a type "List View" and name "list"
    And I create a subview of the view with a type "List View" and name "list2"
    And I visit the view page
    Then I should see "list" view as a subview of the view
    And I should see "list2" view as a subview of the view

 @realupdatecenter
  Scenario: Set default view of a Nested view
   Given I have installed the "nested-view" plugin
   And a simple job
   When I create a view with a type "Nested View" and name "Nested"
   And I create a subview of the view with a type "List View" and name "list"
   And I create a subview of the view with a type "List View" and name "list2"
   And I configure subview "list" as a default of the view
   And I save the view
   And I visit the view page
   Then I should see "list" subview as an active view
   And I should see "list2" subview as an inactive view
