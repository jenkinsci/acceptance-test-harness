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
}
