Feature: Adds Apache Ant support
  In order to be able to build Ant projects
  As a Jenkins user
  I want to install and configure Ant and build Ant based project

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
    When I build the job
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
    And I build the job
    Then I should see console output matching "Unpacking http://archive.apache.org/dist/ant/binaries/apache-ant-1.8.4-bin.zip"
    And the build should succeed

  Scenario: Add locally installed Ant
    Given I have installed the "ant" plugin
    And fake Ant installation at "/tmp/fake-ant"
    And a job
    When I add Ant version with name "local_ant_1.8.4" and Ant home "/tmp/fake-ant" to Jenkins config page
    And I add an Ant build step for:
      """
        <project default="hello">
          <target name="hello">
            <echo message="Hello World"/>
          </target>
        </project>
      """
    And I select Ant named "local_ant_1.8.4"
    And I build the job
    Then I should see console output matching "fake ant at /tmp/fake-ant/bin/ant"
    And the build should succeed
