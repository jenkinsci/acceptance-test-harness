Feature: Use Sciptler plugin
  In order to be able to store, download and share custom scripts
  As a Jenkins user
  I want to use Scriptler plugin

  Scenario: Run new script
    Given I have installed the "scriptler" plugin
    When I upload script "scriptler_plugin/hello_world.groovy"
    And I run the script
    Then the script output should match "Hello world!"

  Scenario: Run new parameterized script
    Given I have installed the "scriptler" plugin
    When I upload script "scriptler_plugin/hello_parameterized.groovy"
    And I add script parameters:
        | noun | space |
    And I run the script
    Then the script output should match "Hello space!"

  Scenario: Delete script
    Given I have installed the "scriptler" plugin
    When I upload script "scriptler_plugin/hello_world.groovy"
    And  I upload script "scriptler_plugin/hello_parameterized.groovy"
    And I delete script "hello_world.groovy"
    Then script "hello_parameterized.groovy" should exist
    But  script "hello_world.groovy" should not exist

  Scenario: Run script on particular slave
    Given I have installed the "scriptler" plugin
    And a slave named "slave42"
    When I upload script "scriptler_plugin/hello_world.groovy"
    And I run the script on slave42
    Then the script output on slave42 should match "Hello world!"
    And  the script should not be run on "master"

  Scenario: Run script on master
    Given I have installed the "scriptler" plugin
    And a slave named "slave42"
    When I upload script "scriptler_plugin/hello_world.groovy"
    And I run the script on master
    Then the script output on master should match "Hello world!"
    And  the script should not be run on slave42

  Scenario: Run script on all nodes
    Given I have installed the "scriptler" plugin
    And a slave named "slave42"
    When I upload script "scriptler_plugin/hello_world.groovy"
    And I run the script on all nodes
    Then the script output on slave42 should match "Hello world!"
    And  the script output on master should match "Hello world!"

  Scenario: Run script on all slaves
    Given I have installed the "scriptler" plugin
    And a slave named "slave42"
    And a slave named "slave43"
    When I upload script "scriptler_plugin/hello_world.groovy"
    And I run the script on all slaves
    Then the script output on slave42 should match "Hello world!"
    And  the script output on slave43 should match "Hello world!"
    And  the script should not be run on master

  Scenario: Create and run parameterized test
    Given I have installed the "scriptler" plugin
    When I create script
        """
            println lhs + ' + ' + rhs;
        """
    And I add script parameters:
        | lhs |  7 |
        | rhs | 11 |
    And I run the script
    Then the script output should match "7 + 11"

  Scenario: Override default parameters
    Given I have installed the "scriptler" plugin
    When I create script
        """
            println lhs + ' + ' + rhs;
        """
    And I add script parameters:
        | lhs |  7 |
        | rhs | 11 |
    And I run parameterized script with:
        | rhs |  9 |
    Then the script output should match "7 + 9"
