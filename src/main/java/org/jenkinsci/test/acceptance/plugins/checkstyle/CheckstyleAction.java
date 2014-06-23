package org.jenkinsci.test.acceptance.plugins.checkstyle;

import org.jenkinsci.test.acceptance.plugins.analysis_core.AbstractCodeStylePluginAction;
import org.jenkinsci.test.acceptance.po.ContainerPageObject;

/**
 * Page object for Checkstyle action.
 */
public class CheckstyleAction extends AbstractCodeStylePluginAction {

    public CheckstyleAction(ContainerPageObject parent) {
        super(parent, "checkstyle");
    }

}
