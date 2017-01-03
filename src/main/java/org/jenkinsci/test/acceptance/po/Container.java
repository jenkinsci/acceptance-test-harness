package org.jenkinsci.test.acceptance.po;

/**
 * A container owns jobs and views. Know container implementations are {@link Jenkins} and {@link Folder}.
 *
 * @author Ullrich Hafner
 */
public interface Container {
    /**
     * Returns the jobs in this container.
     *
     * @return the jobs
     */
    JobsMixIn getJobs();

    /**
     * Returns the views in this container.
     *
     * @return the views
     */
    ViewsMixIn getViews();
}
