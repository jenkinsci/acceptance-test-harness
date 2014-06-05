package org.jenkinsci.test.acceptance.plugins.findbugs;

import org.jenkinsci.test.acceptance.plugins.AbstractCodeStylePluginMavenBuildSettings;
import org.jenkinsci.test.acceptance.plugins.maven.MavenModuleSet;
import org.jenkinsci.test.acceptance.po.Describable;

/**
 * Findbugs build settings for maven projects.
 * @author Fabian Trampusch
 */
@Describable("Publish FindBugs analysis results")
public class FindbugsMavenBuildSettings extends AbstractCodeStylePluginMavenBuildSettings {

    /**
     * Constructor for the build settings page area.
     * @param parent the job currently being configured.
     */
    public FindbugsMavenBuildSettings(MavenModuleSet parent) {
        super(parent, "findbugs-FindBugsReporter");
    }
}
