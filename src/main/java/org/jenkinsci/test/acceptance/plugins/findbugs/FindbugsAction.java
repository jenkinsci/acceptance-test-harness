package org.jenkinsci.test.acceptance.plugins.findbugs;

import org.jenkinsci.test.acceptance.po.ContainerPageObject;
import org.jenkinsci.test.acceptance.plugins.analysis_core.AbstractCodeStylePluginAction;

/**
 * Page object for Findbugs action.
 */
public class FindbugsAction extends AbstractCodeStylePluginAction {
    
    public FindbugsAction(ContainerPageObject parent) {
        super(parent, "findbugs");
    }

}
