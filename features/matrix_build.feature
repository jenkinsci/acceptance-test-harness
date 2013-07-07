Feature: Configure/build matrix jobs

  Scenario: Create a matrix job
    When I create a matrix job named "matrix"
    And I visit the home page
    Then the page should say "matrix"

   Scenario: Run configurations sequentially
    Given a matrix job
    When I configure the job
    And I configure user axis "user_axis" with values "axis1 axis2 axis3"
    And I configure to run configurations sequentially
    And I add a shell build step "sleep 20" in the job configuration
    And I build the job
    Then the configurations should run sequentially

  Scenario: Run a matrix job
    Given a matrix job
    When I configure the job
    And I configure user axis "user_axis" with values "axis1 axis2 axis3"
    And I add a shell build step "ls" in the job configuration
    And I build the job
    Then I shoud see console output of configurations matching "+ ls"

  Scenario: Run build with configuration filter
    Given a matrix job
    When I configure the job
    And I configure user axis "user_axis" with values "axis1 axis2 axis3"
    And I configure combination filter with values "user_axis=='axis2'"
    And I add a shell build step "echo hello" in the job configuration
    And I build the job
    Then combination "user_axis=axis2" should be built
    And combination "user_axis=axis1" should not be built
    And combination "user_axis=axis3" should not be built

  Scenario: Run configurations on with a given label
    Given a matrix job
    When I create dumb slave named "slave"
    And I add the label "label1" to the slave
    And I configure the job
    And I configure slaves axis with value "master"
    And I configure slaves axis with value "label1"
    And I save the job
    And I build the job
    Then the configuration "label=master" should be built on "master"
    And the configuration "label=label1" should be built on "slave"

  Scenario: Run touchstone builds first with resul stable
    Given a matrix job
    When I configure the job
    And I configure user axis "user_axis" with values "axis1 axis2 axis3"
    And I add always fail build step
    And I configure to execute touchstone builds first with filter "user_axis=='axis1'" and required result "UNSTABLE"
    And I save the job
    And I build the job
    Then combination "user_axis=axis2" should not be built
    And combination "user_axis=axis3" should not be built

