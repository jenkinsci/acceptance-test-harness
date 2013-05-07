Feature: Test Gradle plugin
 
  @realupdatecenter
  Scenario: Install Gradle plugin
    Given a job
    When I install the "gradle" plugin from the update center  
    And I configure the job
    Then the job should be able to use the "Invoke Gradle script" buildstep

  @realupdatecenter
  Scenario: Add Auto-Installed Gradle
    Given I have installed the "gradle" plugin
    And I add Gradle version "1.5" with name "gradle-1.5" installed automatically to Jenkins config page
    And a job
    When I configure the job
    And I add script for creating "build.gradle" file :
      """
        task hello {
          doLast {
            println 'Hello world!'
          }
        }
      """
    And I add "Invoke Gradle script" build action
    And I set Gradle version "gradle-1.5", build step description "test" and tasks "hello"
    And I save the job
    And I build the job
    Then the build should succeed
    And I should see console output matching "Hello world!" 
 
 @realupdatecenter
  Scenario: Execute gradle script hello.gradle from gradle directory with quiet switch
    Given I have installed the "gradle" plugin
    And I add Gradle version "1.5" with name "gradle-1.5" installed automatically to Jenkins config page
    And a job
    When I configure the job
    And I add script for creating "hello.gradle" file in directory "gradle" :
      """
        task hello {
          doLast {
            println 'Hello world!'
          }
        }
      """
    And I add "Invoke Gradle script" build action
    And I set Gradle version "gradle-1.5", build step description "test" and tasks "hello"
    And I set Gradle script file name "hello.gradle"
    And I set Gradle script direcotry path "gradle"
    And I set Gradle switches "--quiet"
    And I save the job
    And I build the job
    Then the build should succeed
    And I should see console output matching "gradle --quiet"
    And I should see console output matching "Hello world!"
