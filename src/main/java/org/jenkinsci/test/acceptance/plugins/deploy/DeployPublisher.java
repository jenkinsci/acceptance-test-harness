package org.jenkinsci.test.acceptance.plugins.deploy;

import org.jenkinsci.test.acceptance.po.*;

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
    public final Control user = control("adapters/userName");
    public final Control password = control("adapters/password");
    public final Control url = control("adapters/url");

    public void useContainer(String container) {
        control("hetero-list-add[adapters]").selectDropdownMenu(container);
    }
}
