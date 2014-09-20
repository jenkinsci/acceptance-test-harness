package org.jenkinsci.test.acceptance.plugins.analysis_core;

import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Job;

/**
 * Abstract job configuration class.
 * @author Fabian Trampusch
 */
public abstract class AnalysisFreestyleSettings extends AnalysisSettings {
    /**
     * The input for the file name.
     */
    public final Control pattern = control("pattern");

    /**
     * Constructor for the build settings page area.
     *
     * @param parent       the job currently being configured.
     * @param selectorPath the selector path used as prefix.
     */
    public AnalysisFreestyleSettings(Job parent, String selectorPath) {
        super(parent, selectorPath);
    }
}
