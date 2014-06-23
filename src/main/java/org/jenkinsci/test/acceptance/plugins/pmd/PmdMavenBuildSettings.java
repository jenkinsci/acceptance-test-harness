package org.jenkinsci.test.acceptance.plugins.pmd;

import org.jenkinsci.test.acceptance.plugins.analysis_core.AbstractCodeStylePluginMavenBuildSettings;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.Job;

/**
 * PMD build settings for maven projects.
 * @author Fabian Trampusch
 */
@Describable("Publish PMD analysis results")
public class PmdMavenBuildSettings extends AbstractCodeStylePluginMavenBuildSettings {
    /**
     * Constructor for the build settings page area.
     * @param parent the job currently being configured.
     */
    public PmdMavenBuildSettings(Job parent) {
        super(parent, "pmd-PmdReporter");
    }
}
