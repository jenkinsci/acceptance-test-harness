package org.jenkinsci.test.acceptance.plugins.analysis_core;

import java.util.function.Consumer;

import org.jenkinsci.test.acceptance.po.PostBuildStep;

/**
 * Configures a static analysis plug-in that derives from analysis-core (FindBugs, PMD, etc.).
 *
 * @author Fabian Trampusch
 */
public interface AnalysisConfigurator<T extends PostBuildStep> extends Consumer<T> {
    /**
     * Implement this method to access the static analysis job configuration page area
     * and set e.g. the thresholds as you like.
     *
     * @param settings The settings you can use to configure everything as you like.
     */
    void accept(T settings);
}
