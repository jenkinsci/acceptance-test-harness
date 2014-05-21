package plugins;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.matrix_auth.MatrixAuthorizationStrategy;
import org.jenkinsci.test.acceptance.plugins.matrix_auth.MatrixRow;
import org.jenkinsci.test.acceptance.plugins.mock_security_realm.MockSecurityRealm;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.GlobalSecurityConfig;
import org.jenkinsci.test.acceptance.utils.groovy.InteractiveConsole;
import org.junit.Test;

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
            bob.on(bob.OVERALL_READ);
        }
        sc.save();


        System.out.println();

        jenkins.login().doLogin("alice");

        FreeStyleJob j = jenkins.jobs.create();
        j.save();

        // if we login as Bob, he shouldn't see the job
        jenkins.login().doLogin("bob");
        assertNull(getElement(by.href("job/"+j.name+"/")));

        // contorl assertion: alice shoudl see the link
        jenkins.login().doLogin("alice");
        assertNotNull(getElement(by.href("job/"+j.name+"/")));

        // TODO: variant of href that takes laxed match
    }

}
