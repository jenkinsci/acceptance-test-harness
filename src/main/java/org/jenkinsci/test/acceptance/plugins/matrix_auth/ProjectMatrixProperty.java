package org.jenkinsci.test.acceptance.plugins.matrix_auth;

import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Job;
import org.jenkinsci.test.acceptance.po.PageAreaImpl;
import org.openqa.selenium.WebElement;

/**
 * @author Kohsuke Kawaguchi
 */
public class ProjectMatrixProperty extends PageAreaImpl {

    private final Control useProjectSecurity = control("useProjectSecurity");

    public ProjectMatrixProperty(Job job) {
        super(job, "/properties/hudson-security-AuthorizationMatrixProperty");
    }

    /**
     * Adds a new user/group to this matrix.
     */
    public MatrixRow addUser(String name) {
        WebElement table = waitFor(path("useProjectSecurity/data"));
        runThenHandleInputDialog(() -> table.findElement(by.button("Add user")).click(), name);
        return getUser(name);
    }

    /**
     * Picks up the existing user in the table.
     */
    public MatrixRow getUser(String name) {
        return new MatrixRow(this, "useProjectSecurity/data/USER:" + name);
    }

    /**
     * Enable project based security for this project.
     */
    public void enable() {
        useProjectSecurity.check(true);
    }

    /**
     * Disables project based security for this project.
     */
    public void disable() {
        useProjectSecurity.check(false);
    }
}
