Feature: Notify users via email
  In order to have all contributors informed
  As a Jenkins project manager
  I want to send and configure mail notifications

  Scenario: Send test email
    Given a default mailer setup
    When I send test mail to "admin@example.com"
    Then a mail message "Test email #1" for "admin@example.com" should match
        """
        This is test email #1 sent from Jenkins
        """

  Scenario: Send mail for failed build
    Given a default mailer setup
    And a job
    When I configure the job
    And I add always fail build step
    And I configure mail notification for "dev@example.com mngmnt@example.com"
    And I save the job
    And I build the job
    Then a mail message "Build failed in Jenkins: .* #1" for "dev@example.com mngmnt@example.com" should match
        """
        failure
        """
