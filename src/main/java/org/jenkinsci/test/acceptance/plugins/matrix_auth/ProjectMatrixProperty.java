package org.jenkinsci.test.acceptance.plugins.matrix_auth;

import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Job;
import org.jenkinsci.test.acceptance.po.PageAreaImpl;
import org.openqa.selenium.UnhandledAlertException;

/**
 * @author Kohsuke Kawaguchi
 */
public class ProjectMatrixProperty extends PageAreaImpl {

    private final Control useProjectSecurity = control("useProjectSecurity");

    private final Control table = control("useProjectSecurity/data");

    public ProjectMatrixProperty(Job job) {
        super(job, "/properties/hudson-security-AuthorizationMatrixProperty");
    }

    /**
     * Adds a new user/group to this matrix.
     */
    public MatrixRow addUser(String name) {
        try {
            // 3.2.3 and later
            this.table
                    .resolve()
                    .findElement(
                            by.xpath(
                                    "../div/span/span/button[text()='Add user\u2026'] | ../div/button[text()='Add user\u2026']"))
                    .click();
            getPage().find(by.css("dialog input")).sendKeys(name);
            getPage().find(by.css("dialog .jenkins-button--primary")).click();
        } catch (UnhandledAlertException ex) {
            // 3.2.2 and earlier
            runThenHandleAlert(
                    () -> {
                        this.table
                                .resolve()
                                .findElement(
                                        by.xpath(
                                                "../div/span/span/button[text()='Add user\u2026'] | ../div/button[text()='Add user\u2026']"))
                                .click();
                    },
                    a -> {
                        a.sendKeys(name);
                        a.accept();
                    });
        }
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
