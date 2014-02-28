Feature: Adds support for editable email configuration
  In order to be able to send customized mail notifications
  As a Jenkins user
  I want to install and configure email-ext plugin

  Scenario: Build
    Given I have installed the "email-ext" plugin
    And a default mailer setup
    And a job
    And I add always fail build step
    And I configure editable email "Modified $DEFAULT_SUBJECT" for "dev@example.com"
        """
            $DEFAULT_CONTENT
            with amendment
        """
    And I save the job
    And I build the job
    Then the build should fail
    And a mail message "^Modified " for "dev@example.com" should match
        """
        with amendment$
        """

