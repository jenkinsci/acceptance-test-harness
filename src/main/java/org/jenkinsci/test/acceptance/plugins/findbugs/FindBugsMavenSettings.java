package org.jenkinsci.test.acceptance.plugins.findbugs;

import org.jenkinsci.test.acceptance.plugins.analysis_core.AnalysisMavenSettings;
import org.jenkinsci.test.acceptance.plugins.maven.MavenModuleSet;
import org.jenkinsci.test.acceptance.po.Describable;

/**
 * FindBugs build settings for maven projects.
 *
 * @author Fabian Trampusch
 */
@Describable("Publish FindBugs analysis results")
public class FindBugsMavenSettings extends AnalysisMavenSettings {
    public FindBugsMavenSettings(final MavenModuleSet parent, final String selectorPath) {
        super(parent, selectorPath);
    }
}
