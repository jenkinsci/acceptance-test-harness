package org.jenkinsci.test.acceptance.plugins.jira;

import org.jenkinsci.test.acceptance.po.AbstractStep;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.Job;
import org.jenkinsci.test.acceptance.po.PostBuildStep;

/**
 * @author Kohsuke Kawaguchi
 */
@Describable({"JIRA: Update relevant issues", "Jira: Update relevant issues"})
public class JiraUpdater extends AbstractStep implements PostBuildStep {
    public JiraUpdater(Job parent, String path) {
        super(parent, path);
    }
}
