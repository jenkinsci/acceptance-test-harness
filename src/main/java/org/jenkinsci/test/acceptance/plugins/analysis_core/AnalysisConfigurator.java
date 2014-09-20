package org.jenkinsci.test.acceptance.plugins.analysis_core;

/**
 * Configures a static analysis plug-in that derives from analysis-core (FindBugs, PMD, etc.).
 *
 * @author Fabian Trampusch
 */
public abstract class AnalysisConfigurator<T extends AnalysisSettings> {
    /**
     * Override this method to access the code analyzer job configuration page area and set e.g. the thresholds as you
     * like.
     *
     * @param settings The settings you can use to configure everything as you like.
     */
    public abstract void configure(T settings);
}
