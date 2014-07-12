package org.jenkinsci.test.acceptance.plugins.warnings;

import org.jenkinsci.test.acceptance.plugins.analysis_core.AbstractCodeStylePluginFreestyleBuildSettings;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.Job;

/**
 * Settings of the warnigns plugin. There is no difference between freestyle and maven jobs.
 */
@Describable("Publish compiler warnings analysis results")
public class WarningsBuildSettings extends AbstractCodeStylePluginFreestyleBuildSettings {
    public WarningsBuildSettings(Job parent, String path) {
        super(parent, path);
    }
}
