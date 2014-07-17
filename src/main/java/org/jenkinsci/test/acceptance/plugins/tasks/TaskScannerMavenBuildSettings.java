package org.jenkinsci.test.acceptance.plugins.tasks;

import org.jenkinsci.test.acceptance.plugins.maven.MavenModuleSet;
import org.jenkinsci.test.acceptance.po.Describable;

/**
 * This class provides the ability to add a 'Scan workspace for open tasks'
 * post build step to a Maven 2/3 job.
 *
 * It provides access to the particular controls to configure the post build step.
 * This class derives controls common for the tasks plugin from
 * {@link org.jenkinsci.test.acceptance.plugins.tasks.AbstractTaskScannerBuildSettings}.
 *
 * This post build step requires installation of the tasks plugin.
 *
 * @author Martin Ende
 */
@Describable("Scan workspace for open tasks")
public class TaskScannerMavenBuildSettings extends AbstractTaskScannerBuildSettings {

    public TaskScannerMavenBuildSettings(MavenModuleSet parent, String path) {
        super(parent, path);
    }
}
