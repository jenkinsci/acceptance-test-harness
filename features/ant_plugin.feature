Feature: Adds Apache Ant support
  In order to be able to build Ant projects
  As a Jenkins user
  I want to install and configure Ant and build Ant based project

  Scenario: Configure a job with Ant build steps
    Given I have installed the "ant" plugin
    And a job
    When I configure the job
    And I add an Ant build step
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
    And I have Ant "1.8.4" auto-installation named "ant_1.8.4" configured
    And a job
    When I add an Ant build step for "ant_1.8.4"
      """
        <project default="hello">
          <target name="hello">
            <echo message="Hello World"/>
          </target>
        </project>
      """
    And I build the job
    Then the build should succeed
    And console output should contain
        """
        Unpacking http://archive.apache.org/dist/ant/binaries/apache-ant-1.8.4-bin.zip
        """

  Scenario: Add locally installed Ant
    Given I have installed the "ant" plugin
    And fake Ant installation at "/tmp/fake-ant"
    And a job
    And I have Ant "local_ant_1.8.4" installed in "/tmp/fake-ant" configured
    When I add an Ant build step for "local_ant_1.8.4"
      """
        <project default="hello">
          <target name="hello">
            <echo message="Hello World"/>
          </target>
        </project>
      """
    And I build the job
    Then console output should contain "fake ant at /tmp/fake-ant/bin/ant"
    And the build should succeed
