Feature: Look ma, I can run the same test case!
  To reuse cucumber tests as-is from selenium-tests, I implement
  page objects and step definitions in Java

  Scenario: Create a simple job
    When I create a job named "MAGICJOB"
    And I visit the home page
    Then the page should say "MAGICJOB"
