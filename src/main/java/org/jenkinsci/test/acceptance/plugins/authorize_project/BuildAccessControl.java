package org.jenkinsci.test.acceptance.plugins.authorize_project;

import org.jenkinsci.test.acceptance.po.GlobalSecurityConfig;
import org.jenkinsci.test.acceptance.po.PageAreaImpl;

/**
 * Base type for {@link PageAreaImpl} for Build Access Control.
 */
public class BuildAccessControl extends PageAreaImpl {

    public BuildAccessControl(GlobalSecurityConfig security, String path) {
        super(security, path);
    }
}
