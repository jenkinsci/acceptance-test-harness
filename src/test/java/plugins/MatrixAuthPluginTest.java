package plugins;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.matrix_auth.MatrixAuthorizationStrategy;
import org.jenkinsci.test.acceptance.plugins.matrix_auth.MatrixRow;
import org.jenkinsci.test.acceptance.plugins.matrix_auth.ProjectBasedMatrixAuthorizationStrategy;
import org.jenkinsci.test.acceptance.plugins.matrix_auth.ProjectMatrixProperty;
import org.jenkinsci.test.acceptance.plugins.mock_security_realm.MockSecurityRealm;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.GlobalSecurityConfig;
import org.junit.Test;

import static org.jenkinsci.test.acceptance.plugins.matrix_auth.MatrixRow.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Kohsuke Kawaguchi
 */
@WithPlugins({"matrix-auth","mock-security-realm"})
public class MatrixAuthPluginTest extends AbstractJUnitTest {
    /**
     * Test scenario:
     *
     * - we create admin user "alice" and regular limited user "bob"
     * - alice creates a new project
     * - bob shouldn't be able to see it
     */
    @Test
    public void helloWorld() throws Exception {
        GlobalSecurityConfig sc = new GlobalSecurityConfig(jenkins);
        sc.open();
        {
            MockSecurityRealm ms = sc.useRealm(MockSecurityRealm.class);
            ms.configure("alice","bob");

            MatrixAuthorizationStrategy mas = sc.useAuthorizationStrategy(MatrixAuthorizationStrategy.class);

            MatrixRow a = mas.addUser("alice");
            a.admin();

            MatrixRow bob = mas.addUser("bob");
            bob.on(OVERALL_READ);
        }
        sc.save();

        jenkins.login().doLogin("alice");
        FreeStyleJob j = jenkins.jobs.create();
        j.save();
        jenkins.logout();

        // if we login as Bob, he shouldn't see the job
        jenkins.login().doLogin("bob");
        driver.get(jenkins.getCurrentUrl() + "job/" + j.name + "/");
        assertTrue(driver.getTitle().contains("404"));
        jenkins.logout();

        // control assertion: alice should see the job
        jenkins.login().doLogin("alice");
        driver.get(jenkins.getCurrentUrl() + "job/" + j.name + "/");
        assertTrue(driver.getTitle().contains(j.name));
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
            MockSecurityRealm ms = sc.useRealm(MockSecurityRealm.class);
            ms.configure("alice","bob");

            ProjectBasedMatrixAuthorizationStrategy mas = sc.useAuthorizationStrategy(ProjectBasedMatrixAuthorizationStrategy.class);

            MatrixRow a = mas.addUser("alice");
            a.admin();

            MatrixRow bob = mas.addUser("bob");
            bob.on(OVERALL_READ);
        }
        sc.save();

        jenkins.login().doLogin("alice");

        // just create the job without configuring
        FreeStyleJob j = jenkins.jobs.create();

        // bob shouldn't be able to see it without adding a permission for him
        jenkins.login().doLogin("bob");

        // wait for main panel to appear to make sure page is rendered
        waitFor(by.id("main-panel"), 10);

        // check that the project is visible
        assertNull(getElement(by.href("job/"+j.name+"/")));


        // alice will expose this job to bob
        jenkins.login().doLogin("alice");
        j.configure();
        {
            ProjectMatrixProperty p = new ProjectMatrixProperty(j);
            p.enable.check();
            MatrixRow bob = p.addUser("bob");
            bob.on(ITEM_READ);
        }
        j.save();


        // bob should see this job
        jenkins.login().doLogin("bob");

        // wait for main panel to appear to make sure page is rendered
        waitFor(by.id("main-panel"), 10);

        // Check that project now is visible
        assertTrue("The list of jobs should be bigger than zero", all(by.href("job/"+j.name+"/")).size() > 0);
    }

}
