package org.jenkinsci.test.acceptance.docker.fixtures;

import org.jenkinsci.test.acceptance.docker.DockerContainer;
import org.jenkinsci.test.acceptance.docker.DockerFixture;
import org.jenkinsci.test.acceptance.plugins.subversion.SubversionPluginTestException;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Runs stock svn container.
 *
 * @author Matthias Karl
 */
@DockerFixture(id = "svn", ports = {80, 3690, 22})
public class SvnContainer extends DockerContainer {
    public static final String USER = "user";
    public static final String PWD = "test";

    private static final String PROTOCOL_HTTP = "http://";
    private static final String PROTOCOL_SVN = "svn://";
    private static final String LOCALHOST = "localhost:";
    private static final String UNSAVE_REPO = "/svn";
    private static final String User_PWD_SAVE_REPO = "/svn_pwd";

    /**
     * BaseHttpUrl of the Dockercontainer. Uses http protocol
     *
     * @return URL
     * @throws SubversionPluginTestException e
     */
    public URL getHttpUrl() throws SubversionPluginTestException {
        String url = PROTOCOL_HTTP + LOCALHOST + port(80);
        URL returnUrl = null;
        try {
            returnUrl = new URL(url);
        } catch (MalformedURLException e) {
            SubversionPluginTestException.throwMalformedURL(e, url);
        }
        return returnUrl;
    }

    /**
     * BaseSVNUrl of the Dockercontainer. Users svn Protocol.
     *
     * @return URL
     * @throws SubversionPluginTestException e
     */
    public URI getSvnUrl() throws SubversionPluginTestException {
        String url = PROTOCOL_SVN + LOCALHOST + port(3690);
        URI returnUri = null;
        try {
            returnUri = new URI(url);
        } catch (URISyntaxException e) {
            SubversionPluginTestException.throwMalformedURL(e, url);
        }
        return returnUri;
    }

    /**
     * Http Url to an unsave SVN repo
     *
     * @return URL
     * @throws SubversionPluginTestException e
     */
    public URL getUrlUnsaveRepo() throws SubversionPluginTestException {
        String url = getHttpUrl().toString() + UNSAVE_REPO;
        URL returnUrl = null;
        try {
            returnUrl = new URL(url);
        } catch (MalformedURLException e) {
            SubversionPluginTestException.throwMalformedURL(e, url);
        }
        return returnUrl;
    }

    /**
     * Http Url to an unsave SVN repo at a special revision
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
     * Http Url to an username/password save Repo
     *
     * @return URL
     * @throws SubversionPluginTestException e
     */
    public URL getUrlUserPwdSaveRepo() throws SubversionPluginTestException {
        String url = getHttpUrl().toString() + User_PWD_SAVE_REPO;
        URL returnUrl = null;
        try {
            returnUrl = new URL(url);
        } catch (MalformedURLException e) {
            SubversionPluginTestException.throwMalformedURL(e, url);
        }
        return returnUrl;
    }
}
