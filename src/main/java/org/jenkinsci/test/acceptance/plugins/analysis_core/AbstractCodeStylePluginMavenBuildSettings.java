package org.jenkinsci.test.acceptance.plugins.analysis_core;

import org.jenkinsci.test.acceptance.po.Job;

/**
 * Abstract job configuration class.
 * @author Fabian Trampusch
 */
public abstract class AbstractCodeStylePluginMavenBuildSettings extends AbstractCodeStylePluginBuildSettings {

    /**
     * Constructor for the build settings page area.
     *
     * @param parent       the job currently being configured.
     * @param selectorPath the selector path used as prefix.
     */
    public AbstractCodeStylePluginMavenBuildSettings(Job parent, String selectorPath) {
        super(parent, "/hudson-plugins-" + selectorPath);
    }
}
