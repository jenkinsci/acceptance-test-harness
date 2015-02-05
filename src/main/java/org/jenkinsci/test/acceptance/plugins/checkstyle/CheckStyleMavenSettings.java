package org.jenkinsci.test.acceptance.plugins.checkstyle;

import org.jenkinsci.test.acceptance.plugins.analysis_core.AnalysisMavenSettings;
import org.jenkinsci.test.acceptance.plugins.maven.MavenModuleSet;
import org.jenkinsci.test.acceptance.po.Describable;

/**
 * Checkstyle build settings for maven projects.
 *
 * @author Fabian Trampusch
 */
@Describable("Publish Checkstyle analysis results")
public class CheckStyleMavenSettings extends AnalysisMavenSettings {
    public CheckStyleMavenSettings(final MavenModuleSet parent, final String selectorPath) {
        super(parent, selectorPath);
    }
}
