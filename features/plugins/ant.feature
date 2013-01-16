Feature: Adds Apache Ant support
  In order to be able to build Ant projects
  As a Jenkins user
  I want to install and configure Ant and build Ant based project
  
  Scenario: Install Ant plugin
    When I install the "ant" plugin from the update center
    And I create a job named "ant-test"
    Then the job should be able to use the "Invoke Ant" buildstep

  Scenario: Configure a job with Ant build steps
    Given I have installed the "ant" plugin
    And a job
    When I configure the job
    And I add an Ant build step for:
      """
        <project default="hello">
          <target name="hello">
            <echo message="Hello World"/>
          </target>
        </project>
      """
    When I run the job
    Then the build should succeed

  Scenario: Add Auto-Installed Ant
    Given I have installed the "ant" plugin
    And a job
    When I add Ant version "1.8.4" with name "ant_1.8.4" installed automatically to Jenkins config page
    And I add an Ant build step for:
      """
        <project default="hello">
          <target name="hello">
            <echo message="Hello World"/>
          </target>
        </project>
      """
    And I select Ant named "ant_1.8.4"
    And I run the job
    Then I should see console output matching "Unpacking http://archive.apache.org/dist/ant/binaries/apache-ant-1.8.4-bin.zip"
    And the build should succeed

  @needs_preinstalled_sw
  Scenario: Add locally installed Ant
    Given I have installed the "ant" plugin
    And a job
    When I add Ant version with name "local_ant_1.8.4" and Ant home "/opt/apache/apache-ant-1.8.4/" to Jenkins config page
    And I add an Ant build step for:
      """
        <project default="hello">
          <target name="hello">
            <echo message="Hello World"/>
          </target>
        </project>
      """
    And I select Ant named "local_ant_1.8.4"
    And I run the job
    Then I should see console output matching "/opt/apache/apache-ant-1.8.4/bin/ant"
    And the build should succeed