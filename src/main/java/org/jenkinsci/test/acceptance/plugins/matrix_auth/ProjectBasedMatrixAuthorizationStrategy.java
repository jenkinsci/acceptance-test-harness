package org.jenkinsci.test.acceptance.plugins.matrix_auth;

import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.GlobalSecurityConfig;

/**
 * @author Kohsuke Kawaguchi
 */
@Describable("Project-based Matrix Authorization Strategy")
public class ProjectBasedMatrixAuthorizationStrategy extends MatrixAuthorizationStrategy {
    public ProjectBasedMatrixAuthorizationStrategy(GlobalSecurityConfig context, String path) {
        super(context, path);
    }

    /**
     * Add and authorize given user admin role under "Project-based Matrix Authorization Strategy"
     *
     * @param user     user to be added and authorized as admin
     * @param security page object
     * @return security page object
     */
    public static GlobalSecurityConfig authorizeUserAsAdmin(String user, GlobalSecurityConfig security) {
        ProjectBasedMatrixAuthorizationStrategy auth;
        auth = security.useAuthorizationStrategy(ProjectBasedMatrixAuthorizationStrategy.class);
        MatrixRow userAuth = auth.addUser(user);
        userAuth.admin();
        return security;
    }
}
