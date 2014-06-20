package org.jenkinsci.test.acceptance.plugins.pmd;

import org.jenkinsci.test.acceptance.plugins.AbstractCodeStylePluginFreestyleBuildSettings;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.Job;

/**
 * @author Kohsuke Kawaguchi
 */
@Describable("Publish PMD analysis results")
public class PmdFreestyleBuildSettings extends AbstractCodeStylePluginFreestyleBuildSettings {
    public PmdFreestyleBuildSettings(Job parent, String path) {
        super(parent, path);
    }
}
