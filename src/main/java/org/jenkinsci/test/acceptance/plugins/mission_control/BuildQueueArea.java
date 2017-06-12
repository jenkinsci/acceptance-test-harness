package org.jenkinsci.test.acceptance.plugins.mission_control;

import org.jenkinsci.test.acceptance.po.PageAreaImpl;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * A {@link PageAreaImpl} of the {@link MissionControlView} which offers specific methods to retrieve informations from it.
 */
public class BuildQueueArea extends PageAreaImpl {

    // The parent mission control view of the build history area
    private MissionControlView parent;
    // The basic element of the page area
    public WebElement buildQueue;

    /**
     * Constructor.
     *
     * @param view The parent mission control view.
     */
    public BuildQueueArea(MissionControlView view) {
        super(view, "");
        this.parent = view;
    }

    /**
     * Ensures that the parent {@link MissionControlView} is open. Subsequently, sets the internal buildQueue
     * to eliminate the risk of {@link org.openqa.selenium.StaleElementReferenceException}.
     */
    private void setBuildQueue() {
        parent.ensureViewIsOpen();
        buildQueue = driver.findElement(By.id("jenkinsBuildQueue"));
    }

    /**
     * Determines the current size of the build queue.
     *
     * @return The size of the build queue.
     */
    public int getBuildQueueSize() {
        setBuildQueue();
        return buildQueue.findElements(By.xpath(".//tbody/tr")).size();
    }
}
