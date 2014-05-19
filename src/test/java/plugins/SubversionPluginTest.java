package plugins;

import com.google.inject.Inject;
import org.jenkinsci.test.acceptance.docker.DockerContainerHolder;
import org.jenkinsci.test.acceptance.docker.fixtures.SvnContainer;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.Native;
import org.jenkinsci.test.acceptance.junit.WithCredentials;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.subversion.SubversionPluginTestException;
import org.junit.Test;

/**
 * Feature: Subversion support
 * As a user
 * I want to be able to check out source code from Subversion
 *
 * @author Matthias Karl
 */
@WithPlugins("subversion")
@Native("docker")
public class SubversionPluginTest extends AbstractJUnitTest {
    @Inject
    DockerContainerHolder<SvnContainer> svn;


    /**
     * Scenario:basic Checkout with svn protocol
     * Given I have installed the "subversion" plugin
     * And I have added the right username and password for svn as credentials
     * And a job
     * When I check out code from protected Subversion repository "SvnUrl"
     * And I save the job
     * And I build the job
     * Then the build should succeed
     */
    @Test
    @WithCredentials(credentialType = WithCredentials.USERNAME_PASSWORD, values = {"username", "password"})
    public void run_basic_subversion_build_svn_userPwd() throws SubversionPluginTestException {
        //TODO: add testcases for subversion with credentials plugin
        assertTrue(true);
    }


}
