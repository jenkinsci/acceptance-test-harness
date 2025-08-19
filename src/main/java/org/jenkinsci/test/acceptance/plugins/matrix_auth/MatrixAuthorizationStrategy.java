package org.jenkinsci.test.acceptance.plugins.matrix_auth;

import java.time.Duration;
import org.jenkinsci.test.acceptance.po.AuthorizationStrategy;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.GlobalSecurityConfig;
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.WebElement;

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
        runThenHandleInputDialog(
                () -> {
                    WebElement e = this.table.resolve();
                    waitFor()
                            .withTimeout(Duration.ofSeconds(1))
                            .pollingEvery(Duration.ofMillis(100))
                            .ignoring(ElementNotInteractableException.class)
                            .until(() -> {
                                e.findElement(
                                                by.xpath(
                                                        "../div/span/span/button[text()='Add user\u2026'] | ../div/button[text()='Add user\u2026']"))
                                        .click();
                                return true;
                            });
                },
                name,
                "OK");
        return getUser(name);
    }

    /**
     * Picks up the existing user in the table.
     */
    public MatrixRow getUser(String name) {
        return new MatrixRow(this, "data/USER:" + name);
    }
}
