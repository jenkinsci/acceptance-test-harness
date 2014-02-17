Feature: Executing groovy scripts

  @native(groovy)
  Scenario: Run groovy script
    Given I have installed the "groovy" plugin
    And a job
    And I add groovy script step
        """
           println('hello world')
        """
    And I save the job
    And I build the job
    Then the build should succeed
    And console output should contain "hello world"

  Scenario: Run system groovy script as a command
    Given I have installed the "groovy" plugin
    When I create a job named "system-groovy-test"
    And I add system groovy script step
        """
            j = jenkins.model.Jenkins.instance;
            println 'this is a job ' + j.getItem('system-groovy-test').displayName
        """
    And I save the job
    And I build the job
    Then the build should succeed
    And console output should contain "this is a job system-groovy-test"

  @native(groovy)
  Scenario: Run groovy script from a file
    Given I have installed the "groovy" plugin
    And a job
    When I add a shell build step "echo println \'hello\' > hello.groovy"
    And I add groovy file step "hello.groovy"
    And I save the job
    And I build the job
    Then the build should succeed
    And console output should contain "hello"

  Scenario: Add and run auto-installed Groovy
    Given I have installed the "groovy" plugin
    And I have Groovy "2.1.1" auto-installation named "groovy_2.1.1" configured
    And a job
    When I add groovy script step using "groovy_2.1.1"
        """
            println 'Groovy version: ' + groovy.lang.GroovySystem.getVersion()
        """
    And I save the job
    And I build the job
    Then the build should succeed
    And console output should contain "Groovy version: 2.1.1"
    And console output should contain
        """
        Unpacking http://dist.groovy.codehaus.org/distributions/groovy-binary-2.1.1.zip
        """


  @native(groovy)
  Scenario: Add and run auto-installed Groovy
    Given I have installed the "groovy" plugin
    And fake Groovy installation at "/tmp/fake-groovy"
    And I have Groovy "local_groovy_2.1.1" installed in "/tmp/fake-groovy" configured
    And a job
    When I add groovy script step using "local_groovy_2.1.1"
        """
            println 'Groovy version: ' + groovy.lang.GroovySystem.getVersion()
        """
    And I save the job
    And I build the job
    Then the build should succeed
    And console output should contain "fake groovy at /tmp/fake-groovy/bin/groovy"
