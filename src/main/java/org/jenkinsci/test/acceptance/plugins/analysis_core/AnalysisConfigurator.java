package org.jenkinsci.test.acceptance.plugins.analysis_core;

import java.util.function.Consumer;

import org.jenkinsci.test.acceptance.po.PostBuildStep;

/**
 * Configures a static analysis plug-in that derives from analysis-core (FindBugs, PMD, etc.).
 *
 * @author Fabian Trampusch
 */
public interface AnalysisConfigurator<T extends PostBuildStep> extends Consumer<T> {
    // no additional methods
}
