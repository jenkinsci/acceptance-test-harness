Feature: Adds Scripting support

  Scenario: Execute system script
    When I execute system script
        """
            println Jenkins.instance.displayName;
        """
    Then the system script output should match "Jenkins"

  Scenario: Execute system script on slave
    Given a slave named "my_slave"
    When I execute system script on "my_slave"
        """
            println 6 * 7;
        """
    Then the system script output should match "42"
