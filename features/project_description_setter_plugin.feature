Feature: Set project description based on file in workspace
  In order to be able to change project description dynamically based upon build output
  As a Jenkins user
  I want to be able to set project description using a file in the workspace

  Scenario: Set project description based upon file in workspace
    Given I have installed the "project-description-setter" plugin
    And a job
    When I configure the job
    And I add a shell build step "echo 'Project description setter test' > desc.txt"
    And I setup project description from the file "desc.txt" in workspace
    And I save the job
    And I build the job
    Then the job should have description "Project description setter test"

