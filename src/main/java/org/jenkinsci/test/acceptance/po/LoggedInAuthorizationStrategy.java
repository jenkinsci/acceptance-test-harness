package org.jenkinsci.test.acceptance.po;

/**
 * A logged in user can do everything. Anonymous may have read access.
 *
 * @author Ullrich Hafner
 */
@Describable("Logged-in users can do anything")
public class LoggedInAuthorizationStrategy extends AuthorizationStrategy {
    private final Control name = control("/");
    private final Control allowAnonymousRead = control("allowAnonymousRead");

    public LoggedInAuthorizationStrategy(GlobalSecurityConfig context, String path) {
        super(context, path);
    }

    /**
     * Enables READ access for anonymous.
     */
    public void enableAnonymousReadAccess() {
        setAnonymousAccess(true);
    }

    /**
     * Disables READ access for anonymous.
     */
    public void disableAnonymousReadAccess() {
        setAnonymousAccess(false);
    }

    private void setAnonymousAccess(final boolean state) {
        allowAnonymousRead.check(state);
    }
}
