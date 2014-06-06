package org.jenkinsci.test.acceptance.plugins.checkstyle;

import org.jenkinsci.test.acceptance.plugins.AbstractCodeStylePluginMavenBuildSettings;
import org.jenkinsci.test.acceptance.plugins.maven.MavenModuleSet;
import org.jenkinsci.test.acceptance.po.Describable;

/**
 * Checkstyle build settings for maven projects.
 * @author Fabian Trampusch
 */
@Describable("Publish Checkstyle analysis results")
public class CheckstyleMavenBuildSettings extends AbstractCodeStylePluginMavenBuildSettings {

    /**
     * Constructor for the build settings page area.
     * @param parent the job currently being configured.
     */
    public CheckstyleMavenBuildSettings(MavenModuleSet parent) {
        super(parent, "checkstyle-CheckStyleReporter");
    }
}
