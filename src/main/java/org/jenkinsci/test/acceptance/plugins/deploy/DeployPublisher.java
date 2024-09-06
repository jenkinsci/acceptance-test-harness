package org.jenkinsci.test.acceptance.plugins.deploy;

import org.jenkinsci.test.acceptance.po.AbstractStep;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.Job;
import org.jenkinsci.test.acceptance.po.PostBuildStep;
import org.openqa.selenium.NoSuchElementException;

/**
 * @author Kohsuke Kawaguchi
 */
@Describable("Deploy war/ear to a container")
public class DeployPublisher extends AbstractStep implements PostBuildStep {
    public DeployPublisher(Job parent, String path) {
        super(parent, path);
    }

    public final Control war = control("war");
    public final Control contextPath = control("contextPath");
    public final Control url = control("adapters/url");

    public void setCredentials(String credentials) {
        control("adapters/credentialsId").select(credentials);
    }

    public void useContainer(String... container) {
        RuntimeException last = new RuntimeException("No container names provided");
        for (String c : container) {
            try {
                control("hetero-list-add[adapters]").selectDropdownMenu(c);
                return;
            } catch (NoSuchElementException ex) {
                last = ex;
            }
        }
        throw last;
    }
}
