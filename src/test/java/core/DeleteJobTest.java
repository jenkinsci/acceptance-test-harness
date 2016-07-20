package core;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.jenkinsci.test.acceptance.Matchers.hasContent;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Feature: Delete a job
 * Deletes a job and checks that is sucesfully deleted
 */
public class DeleteJobTest extends AbstractJUnitTest {
    /**
     * Scenario: Delete a single job
     * When I create a job named "simple-job"
     * And I delete the job named "simple-job"
     * Then the job details page should not exist
     */
    @Test
    public void delete_a_simple_job() {
        FreeStyleJob j = jenkins.jobs.create(FreeStyleJob.class, "simple-job");
        assertTrue(j.open().getTitle().contains(j.name));

        j.delete();

        assertFalse(j.open().getTitle().contains(j.name));
    }
}
