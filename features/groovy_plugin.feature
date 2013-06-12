Feature: Executing groovy scripts

 @realupdatecenter
 @needs_preinstalled_sw
  Scenario: Run groovy script
    Given I have installed the "groovy" plugin
    And a job
    When I set groovy build step "Execute Groovy script"
    And I set groovy script "println('hello world')"
    And I save the job
    And I build the job
    Then the build should succeed
    And I should see console output matching "hello world"

 @realupdatecenter
  Scenario: Run system groovy script as a command
    Given I have installed the "groovy" plugin
    When I create a job named "system-groovy-test"
    And I set groovy build step "Execute system Groovy script"
    And I set groovy script "println('this is a job ' + jenkins.model.Jenkins.getInstance().getItem('system-groovy-test').getDisplayName())"
    And I save the job
    And I build the job
    Then the build should succeed
    And I should see console output matching "this is a job system-groovy-test"

  @realupdatecenter
  @needs_preinstalled_sw
  Scenario: Run groovy script from a file
    Given I have installed the "groovy" plugin
    And a job
    When I add a shell build step "echo println \'hello\' > hello.groovy"
    And I set groovy build step "Execute Groovy script"
    And I set groovy script from file "hello.groovy"
    And I save the job
    And I build the job
    Then the build should succeed
    And I should see console output matching "hello"

  @realupdatecenter
  Scenario: Add and run auto-installed Groovy
    Given I have installed the "groovy" plugin
    And a job
    When I add Groovy version "2.1.1" with name "groovy_2.1.1" installed automatically to Jenkins config page
    And I configure the job
    And I set groovy build step "Execute Groovy script"
    And I select groovy named "groovy_2.1.1"
    And I set groovy script "println 'Groovy version: ' + groovy.lang.GroovySystem.getVersion()"
    And I save the job
    And I build the job
    Then the build should succeed
    And I should see console output matching "Unpacking http://dist.groovy.codehaus.org/distributions/groovy-binary-2.1.1.zip"
    And I should see console output matching "Groovy version: 2.1.1"

  @realupdatecenter
  @needs_preinstalled_sw
  Scenario: Add and run auto-installed Groovy
    Given I have installed the "groovy" plugin
    And fake Groovy installation at "/tmp/fake-groovy"
    And a job
    When I add Groovy version with name "local_groovy_2.1.1" and Groovy home "/tmp/fake-groovy" to Jenkins config page
    And I configure the job
    And I set groovy build step "Execute Groovy script"
    And I select groovy named "local_groovy_2.1.1"
    And I set groovy script "println 'Groovy version: ' + groovy.lang.GroovySystem.getVersion()"
    And I save the job
    And I build the job
    Then the build should succeed
    And I should see console output matching "fake groovy at /tmp/fake-groovy/bin/groovy"
