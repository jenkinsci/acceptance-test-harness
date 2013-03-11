Feature: Test Junit test result publisher
 
  Scenario: Publish test results
    When I create a job named "javadoc-test"
    And I configure the job
    And I add a script build step to prepare test results 
     """
       mvn archetype:generate -DgroupId=com.simple.project -DartifactId=simple-project -DinteractiveMode=false
       cd simple-project
       mvn test
     """
    And I set Junit archiver path "simple-project/target/surefire-reports/*.xml"
    And I save the job
    And I build the job
    Then the build should succeed
    Then the job should see "Test Result" action on the build page

  Scenario: Publish test result which passed
    When I create a job named "javadoc-test"
    And I configure the job
    And I add a script build step to prepare test results
     """
       mvn archetype:generate -DgroupId=com.simple.project -DartifactId=simple-project -DinteractiveMode=false
       cd simple-project
       mvn test
     """
    And I set Junit archiver path "simple-project/target/surefire-reports/*.xml"
    And I save the job
    And I build the job
    Then the build should succeed
    And I visit "Test Result" action on build page
    Then the page should say "0 failures" 

 Scenario: Publish test result which failed
    When I create a job named "javadoc-test"
    And I configure the job
    And I add a script build step to prepare test results
     """
       mvn archetype:generate -DgroupId=com.simple.project -DartifactId=simple-project -DinteractiveMode=false
       sed -i "s/true/false/" simple-project/src/test/java/com/simple/project/AppTest.java
       cd simple-project
       mvn test
     """
    And I set Junit archiver path "simple-project/target/surefire-reports/*.xml"
    And I save the job
    And I build the job
    Then the build should not succeed
    And I visit "Test Result" action on build page
    Then the page should say "1 failures" 
