package org.jenkinsci.test.acceptance.plugins.matrix_auth;

import org.jenkinsci.test.acceptance.po.AuthorizationStrategy;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.GlobalSecurityConfig;

/**
 * @author Kohsuke Kawaguchi
 */
@Describable("Matrix-based security")
public class MatrixAuthorizationStrategy extends AuthorizationStrategy {
    private final Control table = control("/data");

    public MatrixAuthorizationStrategy(GlobalSecurityConfig context, String path) {
        super(context, path);
    }

    /**
     * Adds a new user to this matrix.
     */
    public MatrixRow addUser(String name) {
        runThenHandleAlert(() -> this.table.resolve().findElement(by.xpath("../div/span/span/button[text()='Add user\u2026'] | ../div/button[text()='Add user\u2026']")).click(),
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
        return new MatrixRow(this,"data/USER:"+name);
    }
}
