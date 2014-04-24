package org.jenkinsci.test.acceptance.plugins.checkstyle;

import org.jenkinsci.test.acceptance.po.ContainerPageObject;
import org.jenkinsci.test.acceptance.plugins.AbstractCodeStylePluginAction;

import java.net.URL;

/**
 * Page object for Checkstyle action.
 */
public class CheckstyleAction extends AbstractCodeStylePluginAction {

    public CheckstyleAction(ContainerPageObject parent) {
        super(parent, "checkstyle/");
    }

    public URL getHighPrioUrl() {
        return parent.url("checkstyleResult/HIGH");
    }
	
}
