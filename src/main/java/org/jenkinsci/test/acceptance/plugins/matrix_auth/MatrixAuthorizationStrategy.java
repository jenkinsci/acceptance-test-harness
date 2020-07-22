package org.jenkinsci.test.acceptance.plugins.matrix_auth;

import org.jenkinsci.test.acceptance.po.AuthorizationStrategy;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.GlobalSecurityConfig;
import org.openqa.selenium.WebElement;

/**
 * @author Kohsuke Kawaguchi
 */
@Describable("Matrix-based security")
public class MatrixAuthorizationStrategy extends AuthorizationStrategy {
    private final Control name = control("/data");

    public MatrixAuthorizationStrategy(GlobalSecurityConfig context, String path) {
        super(context, path);
    }

    /**
     * Adds a new user/group to this matrix.
     */
    public MatrixRow addUser(String name) {
        runThenHandleAlert(() -> {
            WebElement button = this.name.resolve().findElement(by.parent())
                                         .findElement(by.button("Add user or group…"));
            // JENKINS-63159 if the user has just (un)checked a permission then the button click may fail as the button
            // is obscured by a tooltip
            button.sendKeys(" "); // because this is reliable and button.click() is not due to the tooltip.
        }, a -> {
            a.sendKeys(name);
            a.accept();
        });
        return getUser(name);
    }

    /**
     * Picks up the existing user in the table.
     */
    public MatrixRow getUser(String name) {
        return new MatrixRow(this,"data/"+name);
    }
}
