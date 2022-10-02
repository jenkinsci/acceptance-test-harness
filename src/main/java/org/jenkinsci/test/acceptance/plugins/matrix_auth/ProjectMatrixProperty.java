package org.jenkinsci.test.acceptance.plugins.matrix_auth;

import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Job;
import org.jenkinsci.test.acceptance.po.PageAreaImpl;

/**
 * @author Kohsuke Kawaguchi
 */
public class ProjectMatrixProperty extends PageAreaImpl {
    public final Control enable = control("useProjectSecurity");

    public final Control name = control(/* 2.x */"useProjectSecurity/[1]/data", /* 1.x */"useProjectSecurity/data");

    public ProjectMatrixProperty(Job job) {
        super(job, "/properties/hudson-security-AuthorizationMatrixProperty");
    }

    /**
     * Adds a new user/group to this matrix.
     */
    public MatrixRow addUser(String name) {
        runThenHandleAlert(() -> this.name.resolve().findElement(by.xpath("../div/span/span/button | button")).click(),
                a -> {
            a.sendKeys(name);
            a.accept();
        });
        return getUser(name);
    }

    /**
     * Picks up the existing user in the table.
     */
    public MatrixRow getUser(String name) {
        return new MatrixRow(this,"useProjectSecurity/data/USER:"+name);
    }
}
