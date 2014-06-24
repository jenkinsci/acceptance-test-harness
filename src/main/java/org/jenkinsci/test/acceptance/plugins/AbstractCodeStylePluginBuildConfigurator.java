package org.jenkinsci.test.acceptance.plugins;

/**
 * Configures code style plugin (Findbugs / PMD / ...).
 * @author Fabian Trampusch
 */
public abstract class AbstractCodeStylePluginBuildConfigurator<T extends AbstractCodeStylePluginBuildSettings> {

    /**
     * Override this method to access the code analyzer job configuration page area and set e.g. the thresholds as you like.
     * @param settings The pageArea you can use to configure everything as you like.
     */
    public abstract void configure(T settings);
}
