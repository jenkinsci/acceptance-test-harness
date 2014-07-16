package org.jenkinsci.test.acceptance.plugins.checkstyle;

import org.jenkinsci.test.acceptance.plugins.analysis_core.AbstractCodeStylePluginMavenBuildSettings;
import org.jenkinsci.test.acceptance.plugins.maven.MavenModuleSet;
import org.jenkinsci.test.acceptance.po.Describable;

/**
 * Checkstyle build settings for maven projects.
 * @author Fabian Trampusch
 */
@Describable("Publish Checkstyle analysis results")
public class CheckstyleMavenBuildSettings extends AbstractCodeStylePluginMavenBuildSettings {

    public CheckstyleMavenBuildSettings(MavenModuleSet parent, String selectorPath) {
        super(parent, selectorPath);
    }
}
