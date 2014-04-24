package org.jenkinsci.test.acceptance.plugins.findbugs;

import org.jenkinsci.test.acceptance.po.ContainerPageObject;
import org.jenkinsci.test.acceptance.plugins.AbstractCodeStylePluginAction;
import java.net.URL;

/**
 * Page object for Findbugs action.
 */
public class FindbugsAction extends AbstractCodeStylePluginAction {
    
    public FindbugsAction(ContainerPageObject parent) {
        super(parent, "findbugs/");
    }

    public URL getHighPrioUrl() {
        return parent.url("findbugsResult/HIGH");
    }

}
