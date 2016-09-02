package core;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.jenkinsci.test.acceptance.Matchers.pageObjectExists;
import static org.jenkinsci.test.acceptance.Matchers.pageObjectDoesNotExist;

public class DeleteJobTest extends AbstractJUnitTest {

    @Test
    public void delete_a_simple_job() {
        FreeStyleJob j = jenkins.jobs.create(FreeStyleJob.class, "simple-job");
        assertThat(j, pageObjectExists());

        j.delete();

        elasticSleep(1000); // wait for delete to complete.
        assertThat(j,pageObjectDoesNotExist());
    }
}
