package org.jenkinsci.test.acceptance.docker.fixtures;

import org.jenkinsci.test.acceptance.docker.DockerContainer;
import org.jenkinsci.test.acceptance.docker.DockerFixture;
import org.jenkinsci.test.acceptance.plugins.subversion.SubversionPluginTestException;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Runs stock svn container.
 *
 * @author Matthias Karl
 */
@DockerFixture(id = "svn", ports = 80)
public class SvnContainer extends DockerContainer {
    public static final String USER = "user";
    public static final String PWD = "test";

    private static final String LOCALHOST = "http://localhost:";
    private static final String UNSAVE_REPO = "/svn";
    private static final String User_PWD_SAVE_REPO = "/svn_pw";

    /**
     * BaseUrl of the Dockercontainer
     *
     * @return URL
     * @throws SubversionPluginTestException e
     */
    public URL getUrl() throws SubversionPluginTestException {
        String url = LOCALHOST + port(80);
        URL returnUrl = null;
        try {
            returnUrl = new URL(url);
        } catch (MalformedURLException e) {
            SubversionPluginTestException.throwMalformedURL(e, url);
        }
        return returnUrl;
    }

    /**
     * Url to an unsave SVN repo
     *
     * @return URL
     * @throws SubversionPluginTestException e
     */
    public URL getUrlUnsaveRepo() throws SubversionPluginTestException {
        String url = getUrl().toString() + UNSAVE_REPO;
        URL returnUrl = null;
        try {
            returnUrl = new URL(url);
        } catch (MalformedURLException e) {
            SubversionPluginTestException.throwMalformedURL(e, url);
        }
        return returnUrl;
    }

    /**
     * Url to an unsave SVN repo at a special revision
     *
     * @return URL
     * @throws SubversionPluginTestException e
     */
    public URL getUrlUnsaveRepoAtRevision(int revision) throws SubversionPluginTestException {
        String url = getUrlUnsaveRepo().toString() + "@" + revision;
        URL returnUrl = null;
        try {
            returnUrl = new URL(url);
        } catch (MalformedURLException e) {
            SubversionPluginTestException.throwMalformedURL(e, url);
        }
        return returnUrl;
    }

    /**
     * Url to an username/password save Repo
     *
     * @return URL
     * @throws SubversionPluginTestException e
     */
    public URL getUrlUserPwdSaveRepo() throws SubversionPluginTestException {
        String url = getUrl().toString() + User_PWD_SAVE_REPO;
        URL returnUrl = null;
        try {
            returnUrl = new URL(url);
        } catch (MalformedURLException e) {
            SubversionPluginTestException.throwMalformedURL(e, url);
        }
        return returnUrl;
    }
}
