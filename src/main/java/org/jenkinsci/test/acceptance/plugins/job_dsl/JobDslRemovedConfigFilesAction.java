package org.jenkinsci.test.acceptance.plugins.job_dsl;

/**
 * Actions what to do when a previously generated config file is not referenced anymore.
 *
 * @author Maximilian Oeckler
 */
public enum JobDslRemovedConfigFilesAction {

    IGNORE, DELETE
}
