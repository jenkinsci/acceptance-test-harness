Feature: Use Sciptler plugin
  In order to be able to store, download and share custom scripts
  As a Jenkins user
  I want to use Scriptler plugin

  @realupdatecenter
  Scenario: Run new script
    Given I have installed the "scriptler" plugin
    When I upload script "scriptler_plugin/hello_world.groovy"
    And I run the script
    Then the script output should match "Hello world!"

  @realupdatecenter
  Scenario: Run new parameterized script
    Given I have installed the "scriptler" plugin
    When I upload script "scriptler_plugin/hello_parameterized.groovy"
    And I run the script with:
        | noun | space |
    Then the script output should match "Hello space!"

  @realupdatecenter
  Scenario: Delete script
    Given I have installed the "scriptler" plugin
    When I upload script "scriptler_plugin/hello_world.groovy"
    And  I upload script "scriptler_plugin/hello_parameterized.groovy"
    And I delete script "hello_world.groovy"
    Then script "hello_parameterized.groovy" should exist
    But  script "hello_world.groovy" should not exist

