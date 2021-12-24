package org.jenkinsci.test.acceptance.plugins.external_workspace_manager;

import org.jenkinsci.test.acceptance.po.PageAreaImpl;
import org.jenkinsci.test.acceptance.po.Slave;

/**
 * Helper class for interacting with External Workspace Manager Plugin node config page.
 *
 * @author Alexandru Somai
 */
public class ExternalNodeConfig extends PageAreaImpl {

    public ExternalNodeConfig(Slave context) {
        super(context, "/nodeProperties/org-jenkinsci-plugins-ewm-nodes-ExternalWorkspaceProperty");
    }

    public void setConfig(String diskPoolId, String diskOneId, String diskTwoId, String fakeMountingPoint) {
        // set disk pool
        control(by.checkbox("External Workspace")).click();
        control("repeatable-add").click();
        control("nodeDiskPools/diskPoolRefId").set(diskPoolId);

        // add first disk
        control("nodeDiskPools/repeatable-add").click();
        control("nodeDiskPools/nodeDisks/diskRefId").set(diskOneId);
        control("nodeDiskPools/nodeDisks/nodeMountPoint").set(fakeMountingPoint);

        // add second disk
        control("nodeDiskPools/repeatable-add").click();
        control("nodeDiskPools/nodeDisks[1]/diskRefId").set(diskTwoId);
        control("nodeDiskPools/nodeDisks[1]/nodeMountPoint").set(fakeMountingPoint);
    }
}
