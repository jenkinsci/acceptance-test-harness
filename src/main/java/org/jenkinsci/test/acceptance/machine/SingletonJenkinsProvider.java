package org.jenkinsci.test.acceptance.machine;

import com.google.inject.Inject;
import org.jenkinsci.test.acceptance.controller.JenkinsController;

/**
 * @author Stephen Connolly
 */
public class SingletonJenkinsProvider extends JenkinsProvider {
    private final JenkinsController controller;

    @Inject
    public SingletonJenkinsProvider(JenkinsController controller) {
        this.controller = controller;
    }

    @Override
    public JenkinsController get() {
        if (controller.isRunning()) {
            throw new AssertionError("Singleton already in use");
        }
        return controller;
    }
}
