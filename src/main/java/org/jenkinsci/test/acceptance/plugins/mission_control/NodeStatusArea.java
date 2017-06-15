package org.jenkinsci.test.acceptance.plugins.mission_control;


import org.jenkinsci.test.acceptance.po.PageAreaImpl;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

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
}