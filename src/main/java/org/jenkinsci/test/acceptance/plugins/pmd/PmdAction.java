package org.jenkinsci.test.acceptance.plugins.pmd;

import org.jenkinsci.test.acceptance.plugins.AbstractCodeStylePluginAction;
import org.jenkinsci.test.acceptance.po.ContainerPageObject;

/**
 * Page object for Pmd action.
 */
public class PmdAction extends AbstractCodeStylePluginAction {

    public PmdAction(ContainerPageObject parent) {
        super(parent, "pmd");
    }

}
