package org.jenkinsci.test.acceptance.docker.fixtures;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import org.jenkinsci.test.acceptance.docker.DockerContainer;
import org.jenkinsci.test.acceptance.docker.DockerFixture;
import org.jenkinsci.test.acceptance.plugins.subversion.SubversionPluginTestException;

/**
 * Runs stock svn  container.
 * SVN -version 1.8
 *
 * @author Matthias Karl
 */
@DockerFixture(
        id = "svn",
        ports = {80, 3690, 22})
public class SvnContainer extends DockerContainer {
    public static final String USER = "svnUser";
    public static final String PWD = "test";

    private static final String PROTOCOL_HTTP = "http://";
    private static final String PROTOCOL_SVN = "svn://";
    private static final String UNAUTHENTICATED_REPO_PATH = "/svn/myrepo";
    private static final String AUTHENTICATED_REPO_PATH = "/svn_pwd/myrepo";
    private static final String VIEWVC_PATH = "/viewvc/myrepo/";

    /**
     * BaseHttpUrl of the Dockercontainer. Uses http protocol
     *
     * @return URL
     * @throws SubversionPluginTestException e
     */
    public URL getHttpUrl() throws SubversionPluginTestException {
        String url = PROTOCOL_HTTP + ipBound(80) + ':' + port(80);
        return createUrl(url);
    }

    /**
     * BaseSVNUrl of the Dockercontainer. Users svn Protocol.
     *
     * @return URL
     * @throws SubversionPluginTestException e
     */
    public URI getSvnUrl() throws SubversionPluginTestException {
        String url = PROTOCOL_SVN + ipBound(3690) + ':' + port(3690);
        return createUri(url);
    }

    /**
     * Http Url to an un-restricted SVN repo
     *
     * @return URL
     * @throws SubversionPluginTestException e
     */
    public URL getUrlUnauthenticatedRepo() throws SubversionPluginTestException {
        String url = getHttpUrl().toString() + UNAUTHENTICATED_REPO_PATH;
        return createUrl(url);
    }

    /**
     * Http Url to an un-restricted SVN repo at a special revision
     *
     * @return URL
     * @throws SubversionPluginTestException e
     */
    public URL getUrlUnauthenticatedRepoAtRevision(int revision) throws SubversionPluginTestException {
        String url = getUrlUnauthenticatedRepo().toString() + "@" + revision;
        return createUrl(url);
    }

    /**
     * Http Url to an username/password save Repo
     *
     * @return URL
     * @throws SubversionPluginTestException e
     */
    public URL getUrlAuthenticatedRepo() throws SubversionPluginTestException {
        String url = getHttpUrl().toString() + AUTHENTICATED_REPO_PATH;
        return createUrl(url);
    }

    public URI getUrlViewVC() throws SubversionPluginTestException {
        String url = getHttpUrl().toString() + VIEWVC_PATH;
        return createUri(url);
    }

    private URL createUrl(String url) throws SubversionPluginTestException {
        URL returnUrl = null;
        try {
            returnUrl = new URL(url);
        } catch (MalformedURLException e) {
            SubversionPluginTestException.throwMalformedURL(e, url);
        }
        return returnUrl;
    }

    private URI createUri(String url) throws SubversionPluginTestException {
        URI returnUri = null;
        try {
            returnUri = new URI(url);
        } catch (URISyntaxException e) {
            SubversionPluginTestException.throwMalformedURL(e, url);
        }
        return returnUri;
    }
}
