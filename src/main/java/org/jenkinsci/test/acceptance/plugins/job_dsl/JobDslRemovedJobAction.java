package org.jenkinsci.test.acceptance.plugins.job_dsl;

/**
 * Actions what to do when a previously generated job is not referenced anymore.
 *
 * @author Maximilian Oeckler
 */
public enum JobDslRemovedJobAction {
    IGNORE,
    DISABLE,
    DELETE;
}
