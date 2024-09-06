package org.jenkinsci.test.acceptance.plugins.authorize_project;

import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.GlobalSecurityConfig;
import org.jenkinsci.test.acceptance.po.PageAreaImpl;

/**
 * Base type for {@link PageAreaImpl} for Project Default Build Access Control.
 */
@Describable("Project default Build Authorization")
public class ProjectDefaultBuildAccessControl extends BuildAccessControl {

    public final Control strategy =
            control(by.path("/jenkins-security-QueueItemAuthenticatorConfiguration/authenticators/"));
    public final Control userId = control(by.name("_.userid"));

    public ProjectDefaultBuildAccessControl(GlobalSecurityConfig security, String path) {
        super(security, path);
    }

    public ProjectDefaultBuildAccessControl runAsSpecificUser(final String user) {
        strategy.select("Run as Specific User");
        userId.waitFor();
        userId.sendKeys(user);
        return this;
    }

    /**
     * Run a build as the user who triggered it.
     * @return The Project Default Build Access Control.
     */
    public ProjectDefaultBuildAccessControl runAsUserWhoTriggered() {
        strategy.select("Run as User who Triggered Build");
        return this;
    }
}
