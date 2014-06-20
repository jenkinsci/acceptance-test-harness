package org.jenkinsci.test.acceptance.plugins.findbugs;

import org.jenkinsci.test.acceptance.plugins.AbstractCodeStylePluginAction;
import org.jenkinsci.test.acceptance.po.ContainerPageObject;

/**
 * Page object for Findbugs action.
 */
public class FindbugsAction extends AbstractCodeStylePluginAction {
    
    public FindbugsAction(ContainerPageObject parent) {
        super(parent, "findbugs");
    }

}
