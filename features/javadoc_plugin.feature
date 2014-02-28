Feature: Test Javadoc plugin

  Scenario: Publish javadoc from freestyle job
    Given a Maven
    And a job
    When I configure the job
    And I add build steps to generate javadoc
    And I add a Publish javadoc post build step with path "my-app/target/site/apidocs/"
    And I save the job
    And I build the job
    Then the build should succeed
    And the job should have "Javadoc" action
    And the javadoc should display "com.mycompany.app"

  Scenario: Publish javadoc from matrix job
    Given a Maven
    And a matrix job
    When I configure the job
    And I add build steps to generate javadoc
    And I add a Publish javadoc post build step with path "my-app/target/site/apidocs/"
    And I save the job
    And I build the job
    Then javadoc should display "com.mycompany.app" for default configuration
