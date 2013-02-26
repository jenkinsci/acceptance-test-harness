Feature: Executing groovy scripts

  @realupdatecenter
  Scenario: Install Groovy plugin
    When I install the "groovy" plugin from the update center
    And I create a job named "groovy-test"
    Then the job should be able to use the "Execute Groovy script" buildstep
    And  the job should be able to use the "Execute system Groovy script" buildstep

 @realupdatecenter
  Scenario: Run groovy script
    Given I have installed the "groovy" plugin as a command
    And a job
    When I set groovy build step "Execute Groovy script"
    And I set groovy script "println('hello world')"
    And I save the job
    And I build 1 jobs
    Then the build should succeed
    And I should see console output matching "hello world"

 @realupdatecenter
  Scenario: Run system groovy script as a command
    Given I have installed the "groovy" plugin
    When I create a job named "system-groovy-test"
    And I set groovy build step "Execute system Groovy script"
    And I set groovy script "println('this is a job ' + jenkins.model.Jenkins.getInstance().getItem('system-groovy-test').getDisplayName())"
    And I save the job
    And I build 1 jobs
    Then the build should succeed
    And I should see console output matching "this is a job system-groovy-test"

@realupdatecenter
  Scenario: Run groovy script from a file
    Given I have installed the "groovy" plugin
    And a job
    When I add a script build step to run "echo println \'hello\' > hello.groovy"
    And I set groovy build step "Execute Groovy script"
    And I set groovy script from file "hello.groovy"
    And I save the job
    And I build 1 jobs
    Then the build should succeed
    And I should see console output matching "hello"
