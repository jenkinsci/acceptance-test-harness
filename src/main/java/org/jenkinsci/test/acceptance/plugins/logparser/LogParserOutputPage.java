package org.jenkinsci.test.acceptance.plugins.logparser;

import org.jenkinsci.test.acceptance.po.PageObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * The logparser plugin uses two frames.
 *
 * To check the content of the frames the selenium driver must be switched to the frames.
 *
 * @author Michael Engel.
 */
public class LogParserOutputPage extends PageObject {

    /**
     * Defines the different types of frames used by the parsed output of the logparser
     */
    public enum LogParserFrame {
        SIDEBAR("sidebar"), CONTENT("content");

        private String frameName;
        LogParserFrame(String frameName) {
            this.frameName = frameName;
        }

        public String getFrameName() {
            return this.frameName;
        }
    }

    /**
     * Constructor.
     *
     * @param po The page of the logparser.
     */
    public LogParserOutputPage(PageObject po) {
        super(po.injector, po.url(""));
    }

    /**
     * Switch focus back to the default content.
     */
    private void switchToDefaultContent() {
        driver.switchTo().defaultContent();
    }

    /**
     * Switch focus of the driver to the main frame.
     */
    private void switchToMainframe() {
        WebElement e = driver.findElement(By.xpath("//div[@id='main-panel']//table//tbody//tr//td//iframe"));
        driver.switchTo().frame(e);
    }
    
    /**
     * Switch focus of the driver to the specified frame. 
     * Calls {@link LogParserOutputPage#switchToMainframe()} before switching to the specified frame. 
     * 
     * @param frame The frame to switch to. 
     */
    private void switchToFrame(LogParserFrame frame) {
        switchToMainframe();
        WebElement e = driver.findElement(By.xpath("//frame[@name='" + frame.getFrameName() + "']"));
        driver.switchTo().frame(e);
    }
    
    /**
     * Executes the specified action within the context of the target frame and, eventually, restores the context. 
     * Assertions can be made within the action. 
     * 
     * @param targetFrame The frame where the action is performed. 
     * @param action The action which is performed in the context of the target frame. 
     */
    public void executeInFrame(LogParserFrame targetFrame, Runnable action) {
        try{
            switchToFrame(targetFrame);
            action.run();
        }
        finally{
            switchToDefaultContent();
        }
    }
}
