package org.jenkinsci.test.acceptance.plugins.mission_control;


import org.jenkinsci.test.acceptance.po.PageAreaImpl;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * A {@link PageAreaImpl} of the {@link MissionControlView} which offers specific methods to retrieve informations from it.
 */
public class NodeStatusArea extends PageAreaImpl {

    // The parent mission control view of the build history area
    private MissionControlView parent;
    // The basic element of the page area
    public WebElement nodeStatuses;

    /**
     * Constructor.
     *
     * @param view The parent mission control view.
     */
    public NodeStatusArea(MissionControlView view){
        super(view, "");
        this.parent = view;
    }

    /**
     * Ensures that the parent {@link MissionControlView} is open. Subsequently, sets the internal nodeStatuses
     * to eliminate the risk of {@link org.openqa.selenium.StaleElementReferenceException}.
     */
    private void setNodeStatuses() {
        parent.ensureViewIsOpen();
        nodeStatuses = driver.findElement(By.id("jenkinsNodeStatuses"));
    }

    /**
     * Determines the current number of nodes.
     *
     * @return The current number of nodes.
     */
    public int getNumberOfNodes(){
        setNodeStatuses();
        return nodeStatuses.findElements(By.xpath("//button")).size();
    }

    /**
     * Retrieves a node by name from the node container
     *
     * @param nodename The name of the node.
     * @return A single node entry.
     */
    public WebElement getNodeByName(String nodename){
        setNodeStatuses();
        return nodeStatuses.findElement(By.xpath("//button[text()='" + nodename + "']"));
    }

    /**
     * Retrieves the status of a node, which is indicated by the class-attribute.
     *
     * @param nodename The name of the node.
     * @return The class-attribute, which contains the current status of the node.
     */
    public String getStatusOfNode(String nodename){
        setNodeStatuses();
        WebElement e = nodeStatuses.findElement(By.xpath("//button[text()='" + nodename + "']"));
        return e.getAttribute("class");
    }
}