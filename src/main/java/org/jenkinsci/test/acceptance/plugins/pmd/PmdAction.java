package org.jenkinsci.test.acceptance.plugins.pmd;

import org.jenkinsci.test.acceptance.po.ContainerPageObject;
import org.jenkinsci.test.acceptance.plugins.AbstractCodeStylePluginAction;

import java.net.URL;

/**
 * Page object for Pmd action.
 */
public class PmdAction extends AbstractCodeStylePluginAction {

    public PmdAction(ContainerPageObject parent) {
        super(parent, "pmd/");
    }

    public URL getHighPrioUrl() {
        return parent.url("pmdResult/HIGH");
    }

}
