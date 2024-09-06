package org.jenkinsci.test.acceptance.plugins.subversion;

/**
 * Encapsulates the Exceptions thrown by the SVN plugin
 *
 * @author Matthias Karl
 */
public class SubversionPluginTestException extends Exception {

    private static final String REPO_MAY_NOT_BE_PROTECTED =
            "Could not find the link to the credential page. Maybe the svn-pluginversion is not 1.54 or the repository is not protected.";
    private static final String MALFORMED_URL = "The URL %s seems to be malformed.";
    private static final String COULD_NOT_DETERMINE_POPUP_WINDOW =
            "Could not determine the popup window handle. There seem to be more than two windows handled by the driver at the moment.";

    public SubversionPluginTestException(String message, Throwable cause) {
        super(message, cause);
    }

    public SubversionPluginTestException(String message) {
        super(message);
    }

    /**
     * Could not find the link to the credential page. The Repository may not be protected.
     *
     * @param cause Exception e
     * @throws SubversionPluginTestException e
     */
    public static void throwRepoMayNotBeProtected(Exception cause) throws SubversionPluginTestException {
        throw new SubversionPluginTestException(REPO_MAY_NOT_BE_PROTECTED, cause);
    }

    /**
     * The URL to the credential page seems to be malformed.
     *
     * @param cause Exception e
     * @throws SubversionPluginTestException s
     */
    public static void throwMalformedURL(Exception cause, String url) throws SubversionPluginTestException {
        throw new SubversionPluginTestException(String.format(MALFORMED_URL, url), cause);
    }

    /**
     * The URL to the credential page seems to be malformed.
     *
     * @throws SubversionPluginTestException s
     */
    public static void throwCouldNotDeterminePopupWindow() throws SubversionPluginTestException {
        throw new SubversionPluginTestException(COULD_NOT_DETERMINE_POPUP_WINDOW);
    }
}
