Feature: Use Sciptler plugin
  In order to be able to store, download and share custom scripts
  As a Jenkins user
  I want to use Scriptler plugin

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
