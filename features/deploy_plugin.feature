Feature: Auto-deployment to application server via deploy plugin
  In order to get rapid feedback on applications under the development,
  As a Jenkins user
  I want to automate the delivery of web applications

  @native(docker)
  Scenario: Deploy sample webapp to Tomcat7
    Given I have installed the "deploy" plugin
    And a docker fixture "tomcat7"
    And a job
    When I configure the job
    And I add a shell build step
    """
      [ -d my-webapp ] || mvn archetype:generate -DgroupId=com.mycompany.app -DartifactId=my-webapp -DarchetypeArtifactId=maven-archetype-webapp
      cd my-webapp
      mvn install
    """
    And I deploy "my-webapp/target/*.war" to docker tomcat7 fixture at context path "test"
    And I save the job
    And I build the job
    Then the build should succeed
    And console output should match "to container Tomcat 7.x Remote"
    And docker tomcat7 fixture should show "Hello World!" at "/test/"

    When I configure the job
    And I change a shell build step to "cd my-webapp && echo '<html><body>Hello Jenkins</body></html>' > src/main/webapp/index.jsp && mvn install"
    And I save the job
    When I build the job
    Then the build should succeed
    And console output should match "Redeploying"
    And docker tomcat7 fixture should show "Hello Jenkins" at "/test/"


