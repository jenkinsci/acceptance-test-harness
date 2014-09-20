package org.jenkinsci.test.acceptance.plugins.findbugs;

import org.jenkinsci.test.acceptance.plugins.analysis_core.AnalysisMavenSettings;
import org.jenkinsci.test.acceptance.plugins.maven.MavenModuleSet;
import org.jenkinsci.test.acceptance.po.Describable;

/**
 * Findbugs build settings for maven projects.
 * @author Fabian Trampusch
 */
@Describable("Publish FindBugs analysis results")
public class FindbugsMavenBuildSettings extends AnalysisMavenSettings {

    public FindbugsMavenBuildSettings(MavenModuleSet parent, String selectorPath) {
        super(parent, selectorPath);
    }
}
