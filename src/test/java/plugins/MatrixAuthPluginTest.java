package plugins;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.matrix_auth.MatrixAuthorizationStrategy;
import org.jenkinsci.test.acceptance.plugins.matrix_auth.MatrixRow;
import org.jenkinsci.test.acceptance.plugins.matrix_auth.ProjectBasedMatrixAuthorizationStrategy;
import org.jenkinsci.test.acceptance.plugins.matrix_auth.ProjectMatrixProperty;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.GlobalSecurityConfig;
import org.jenkinsci.test.acceptance.po.JenkinsDatabaseSecurityRealm;
import org.junit.Test;

/**
 * @author Kohsuke Kawaguchi
 */
@WithPlugins({"matrix-auth"})
public class MatrixAuthPluginTest extends AbstractJUnitTest {
    /**
     * Test scenario:
     * <p>
     * - we create admin user "alice" and regular limited user "bob"
     * - alice creates a new project
     * - bob shouldn't be able to see it
     */
    @Test
    public void helloWorld() throws Exception {
        GlobalSecurityConfig sc = new GlobalSecurityConfig(jenkins);
        sc.open();
        {
            JenkinsDatabaseSecurityRealm ms = sc.useRealm(JenkinsDatabaseSecurityRealm.class);
            ms.allowUsersToSignUp(true);
            sc.save();
            ms.signup("alice");
            ms.signup("bob");
        }
        {
            sc.open();
            MatrixAuthorizationStrategy mas = sc.useAuthorizationStrategy(MatrixAuthorizationStrategy.class);

            MatrixRow a = mas.addUser("alice");
            a.admin();

            MatrixRow bob = mas.addUser("bob");
            bob.on(MatrixRow.OVERALL_READ);
        }
        sc.save();

        jenkins.login().doLogin("alice");

        FreeStyleJob j = jenkins.jobs.create();

        j.save();

        jenkins.logout();

        // if we login as Bob, he shouldn't see the job
        jenkins.login().doLogin("bob");

        // check for job's existence
        assertFalse(j.open().getTitle().contains(j.name));

        jenkins.logout();

        // control assertion: alice should see the link
        jenkins.login().doLogin("alice");

        assertTrue(j.open().getTitle().contains(j.name));
    }

    /**
     * Test scenario:
     *
     */
    @Test
    public void projectMatrixAuth() throws Exception {
        GlobalSecurityConfig sc = new GlobalSecurityConfig(jenkins);
        sc.open();
        {
            JenkinsDatabaseSecurityRealm ms = sc.useRealm(JenkinsDatabaseSecurityRealm.class);
            ms.allowUsersToSignUp(true);
            sc.save();
            ms.signup("alice");
            ms.signup("bob");
        }
        {
            sc.open();

            ProjectBasedMatrixAuthorizationStrategy mas =
                    sc.useAuthorizationStrategy(ProjectBasedMatrixAuthorizationStrategy.class);

            MatrixRow a = mas.addUser("alice");
            a.admin();

            MatrixRow bob = mas.addUser("bob");
            bob.on(MatrixRow.OVERALL_READ);
        }
        sc.save();

        jenkins.login().doLogin("alice");

        // just create the job without configuring
        FreeStyleJob j = jenkins.jobs.create();

        jenkins.logout();

        // bob shouldn't be able to see it without adding a permission for him
        jenkins.login().doLogin("bob");

        // check for job's existence
        assertFalse(j.open().getTitle().contains(j.name));

        jenkins.logout();

        // alice will expose this job to bob
        jenkins.login().doLogin("alice");
        j.configure();
        {
            ProjectMatrixProperty p = new ProjectMatrixProperty(j);
            p.enable();
            MatrixRow bob = p.addUser("bob");
            bob.on(MatrixRow.ITEM_READ);
        }
        j.save();

        jenkins.logout();

        // bob should see this job
        jenkins.login().doLogin("bob");

        assertTrue(j.open().getTitle().contains(j.name));

        // Switch back to an admin user so that CspRule can check for violations.
        jenkins.logout();
        jenkins.login().doLogin("alice");
    }
}
