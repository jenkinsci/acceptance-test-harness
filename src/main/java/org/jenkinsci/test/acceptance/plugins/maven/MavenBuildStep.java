package org.jenkinsci.test.acceptance.plugins.maven;

import org.jenkinsci.test.acceptance.po.AbstractStep;
import org.jenkinsci.test.acceptance.po.BuildStep;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.jenkinsci.test.acceptance.po.Job;

/**
 * @author Kohsuke Kawaguchi
 */
@Describable("Invoke top-level Maven targets")
public class MavenBuildStep extends AbstractStep implements BuildStep {
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

    public void setGoals(final String goals) {
        targets.set(goals);
    }

    public MavenBuildStep properties(String properties, boolean inject) {
        ensureAdvanced();
        control("properties").set(properties);
        if (inject) {
            control("injectBuildVariables").check(true);
        }
        return this;
    }

    private void ensureAdvanced() {
        if (advancedButton == null) {
            return;
        }

        advancedButton.click();
        advancedButton = null;
    }

    /**
     * Use the default maven version for a job. Note that this maven version needs to be installed before
     * this method is called.
     *
     * @see MavenInstallation#ensureThatMavenIsInstalled(Jenkins)
     * @see MavenInstallation#installSomeMaven(Jenkins)
     */
    public void useDefaultMavenVersion() {
        version.select(MavenInstallation.DEFAULT_MAVEN_ID);
    }
}
