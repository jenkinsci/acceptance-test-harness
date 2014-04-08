package org.jenkinsci.test.acceptance.plugins.maven;

import org.jenkinsci.test.acceptance.po.BuildStep;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.Job;

/**
 * @author Kohsuke Kawaguchi
 */
@Describable("Invoke top-level Maven targets")
public class MavenBuildStep extends BuildStep {
    public final Control version = control("name");
    public final Control targets = control("targets");

    private Control advancedButton = control("advanced-button");

    public MavenBuildStep(Job parent, String path) {
        super(parent, path);
    }

    public MavenBuildStep useLocalRepository() {
        ensureAdvanced();
        control("usePrivateRepository").check();
        return this;
    }

    public MavenBuildStep properties(String properties) {
        ensureAdvanced();
        control("properties").set(properties);
        return this;
    }

    private void ensureAdvanced() {
        if (advancedButton == null) return;

        advancedButton.click();
        advancedButton = null;
    }
}
