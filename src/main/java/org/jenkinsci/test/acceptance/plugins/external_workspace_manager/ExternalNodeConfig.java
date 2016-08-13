package org.jenkinsci.test.acceptance.plugins.external_workspace_manager;

import org.jenkinsci.test.acceptance.po.PageAreaImpl;
import org.jenkinsci.test.acceptance.po.PageObject;

/**
 * Helper class for interacting with External Workspace Manager Plugin node config page.
 *
 * @author Alexandru Somai
 */
public class ExternalNodeConfig extends PageAreaImpl {

    public ExternalNodeConfig(PageObject context) {
        super(context, "/nodeProperties/org-jenkinsci-plugins-ewm-nodes-ExternalWorkspaceProperty");
    }

    public void setConfig(String diskPoolId, String diskOneId, String diskTwoId, String fakeMountingPoint) {
        // set disk pool
        control("").click();
        control("diskPoolRefId").set(diskPoolId);

        // add first disk
        control("repeatable-add").click();
        control("diskNodes/diskRefId").set(diskOneId);
        control("diskNodes/localRootPath").set(fakeMountingPoint);

        // add second disk
        control("repeatable-add").click();
        control("diskNodes[1]/diskRefId").set(diskTwoId);
        control("diskNodes[1]/localRootPath").set(fakeMountingPoint);
    }
}
