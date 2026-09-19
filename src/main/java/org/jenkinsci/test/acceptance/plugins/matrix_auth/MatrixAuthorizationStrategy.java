package org.jenkinsci.test.acceptance.plugins.matrix_auth;

import org.jenkinsci.test.acceptance.po.AuthorizationStrategy;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.GlobalSecurityConfig;
import org.openqa.selenium.WebElement;

/**
 * @author Kohsuke Kawaguchi
 */
@Describable("Matrix-based security")
public class MatrixAuthorizationStrategy extends AuthorizationStrategy {

    public MatrixAuthorizationStrategy(GlobalSecurityConfig context, String path) {
        super(context, path);
    }

    /**
     * Adds a new user to this matrix.
     */
    public MatrixRow addUser(String name) {
        WebElement table = waitFor(path("data"));
        runThenHandleInputDialog(() -> table.findElement(by.button("Add user")).click(), name);
        return getUser(name);
    }

    /**
     * Picks up the existing user in the table.
     */
    public MatrixRow getUser(String name) {
        return new MatrixRow(this, "data/USER:" + name);
    }
}
