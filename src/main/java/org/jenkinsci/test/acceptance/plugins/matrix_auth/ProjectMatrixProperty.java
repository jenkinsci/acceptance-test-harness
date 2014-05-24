package org.jenkinsci.test.acceptance.plugins.matrix_auth;

import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Job;
import org.jenkinsci.test.acceptance.po.PageArea;

/**
 * @author Kohsuke Kawaguchi
 */
public class ProjectMatrixProperty extends PageArea {
    public final Control enable = control("useProjectSecurity");

    public final Control name = control("useProjectSecurity/");

    public ProjectMatrixProperty(Job job) {
        super(job, "/properties/hudson-security-AuthorizationMatrixProperty");
    }

    /**
     * Adds a new user/group to this matrix.
     */
    public MatrixRow addUser(String name) {
        this.name.set(name);
        this.name.resolve().findElement(by.parent()).findElement(by.button("Add")).click();
        return getUser(name);
    }

    /**
     * Picks up the existing user in the table.
     */
    public MatrixRow getUser(String name) {
        return new MatrixRow(this,"useProjectSecurity/data/"+name);
    }
}
