package plugins;

import com.google.inject.Inject;
import org.jenkinsci.test.acceptance.docker.Docker;
import org.jenkinsci.test.acceptance.docker.fixtures.JiraContainer;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.Native;
import org.junit.Test;

/**
 Feature: Update JIRA tickets when a build is ready
   In order to notify people waiting for a bug fix
   As a Jenkins developer
   I want JIRA issues to be updated when a new build is made
 */
public class JiraPluginTest extends AbstractJUnitTest {
    @Inject
    Docker docker;

    /**
     @native(docker)
     Scenario: JIRA ticket gets updated with a build link
       Given a docker fixture "jira"
       And "ABC" project on docker jira fixture
       And a new issue in "ABC" project on docker jira fixture
       And a new issue in "ABC" project on docker jira fixture
       And I have installed the "jira" plugin

       Given I have installed the "git" plugin
       And an empty test git repository
       And a job

       Then I configure docker fixture as JIRA site

       Then I configure the job
       And I check out code from the test Git repository
       And I add "Update relevant JIRA issues" post-build action
       And I save the job

       Then I commit "initial commit" to the test Git repository
       And I build the job
       And the build should succeed

       When I commit "[ABC-1] fixed" to the test Git repository
       And I commit "[ABC-2] fixed" to the test Git repository
       And I build the job
       Then the build should succeed
       And the build should link to JIRA ABC-1 ticket
       And the build should link to JIRA ABC-2 ticket
       And JIRA ABC-1 ticket has comment from admin that refers to the build
     */
    @Test @Native("docker")
    public void jira_ticket_gets_updated_with_a_build_link() throws Exception {
        JiraContainer jira = docker.start(JiraContainer.class);
        jira.waitForReady(this);
        jira.createProject("ABC");
        jira.createIssue("ABC");
        jira.createIssue("ABC");
    }

}
