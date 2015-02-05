package org.jenkinsci.test.acceptance.plugins.tasks;

import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.Job;

/**
 * This class provides the ability to add a 'Scan workspace for open tasks'
 * post build step to a Freestyle job.
 *
 * It provides access to the particular controls to configure the post build step.
 * This class derives controls common for the tasks plugin from
 * {@link org.jenkinsci.test.acceptance.plugins.tasks.AbstractTaskScannerBuildSettings}
 * and adds specific controls for Freestyle jobs.
 *
 * This post build step requires installation of the tasks plugin.
 *
 * @author Martin Ende
 */

@Describable("Scan workspace for open tasks")
public class TasksFreestyleSettings extends AbstractTaskScannerBuildSettings {
    protected Control defaultEncoding = control("defaultEncoding");

    public TasksFreestyleSettings(Job parent, String path) { super(parent, path); }

    /**
     * Sets the input for a different default file encoding
     */
    public void setDefaultEncoding(String defaultEncoding) {
        ensureAdvancedClicked();
        this.defaultEncoding.set(defaultEncoding);
    }
}
