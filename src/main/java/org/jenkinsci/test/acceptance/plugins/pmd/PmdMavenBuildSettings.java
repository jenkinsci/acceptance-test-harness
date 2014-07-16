package org.jenkinsci.test.acceptance.plugins.pmd;

import org.jenkinsci.test.acceptance.plugins.analysis_core.AbstractCodeStylePluginMavenBuildSettings;
import org.jenkinsci.test.acceptance.plugins.maven.MavenModuleSet;
import org.jenkinsci.test.acceptance.po.Describable;

/**
 * PMD build settings for maven projects.
 * @author Fabian Trampusch
 */
@Describable("Publish PMD analysis results")
public class PmdMavenBuildSettings extends AbstractCodeStylePluginMavenBuildSettings {

    public PmdMavenBuildSettings(MavenModuleSet parent, String selectorPath) {
        super(parent, selectorPath);
    }
}
