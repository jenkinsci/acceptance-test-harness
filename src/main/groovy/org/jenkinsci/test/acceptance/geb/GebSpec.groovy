package org.jenkinsci.test.acceptance.geb

import org.jenkinsci.test.acceptance.junit.JenkinsAcceptanceTestRule
import org.jenkinsci.test.acceptance.junit.Resource
import org.jenkinsci.test.acceptance.po.Jenkins
import org.junit.Rule

import javax.inject.Inject

/**
 * Base Class for all geb spock tests.
 *
 * @author christian.fritz
 */
class GebSpec extends geb.spock.GebSpec {

    @Rule
    public GebBrowserRule browserRule = new GebBrowserRule();

    @Rule
    public JenkinsAcceptanceTestRule env = new JenkinsAcceptanceTestRule();

    /**
     * Jenkins under test.
     */
    @Inject
    public Jenkins jenkins;

    /**
     * Obtains a resource in a wrapper.
     */
    public Resource resource(String path) {
        final URL resource = getClass().getResource(path);
        if (resource == null) {
            throw new AssertionError("No such resource " + path + " for " + getClass().getName());
        }
        return new Resource(resource);
    }
}
