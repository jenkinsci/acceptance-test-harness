Feature: Adds Apache Ant support
  In order to be able to build Ant projects
  As a Jenkins user
  I want to install and configure Ant and build Ant based project

  @wip
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
