package org.jenkinsci.test.acceptance.plugins.analysis_core;

import org.jenkinsci.test.acceptance.plugins.maven.MavenModuleSet;

/**
 * Abstract job configuration class.
 *
 * @author Fabian Trampusch
 */
public abstract class AnalysisMavenSettings extends AnalysisSettings {

    /**
     * Constructor for the build settings page area.
     *
     * @param parent       the job currently being configured.
     */
    public AnalysisMavenSettings(MavenModuleSet parent, String selectorPath) {
        super(parent, selectorPath);
    }
}
