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


    // URL to return from the opened frame to the default window
    private String restoreURL = null;

    /**
     * Opens the defined frame in the current window.
     *
     * @param frame Enum of the frame to open in the current window.
     */
    public void openFrameInWindow(LogParserFrame frame) {
        switchToMainframe();
        WebElement e = driver.findElement(By.xpath("//frame[@name='" + frame.getFrameName() + "']"));
        restoreURL = driver.getCurrentUrl();
        driver.navigate().to(e.getAttribute("src"));
    }

    /**
     * Returns to the previous window.
     */
    public void restoreWindow() {
        if(restoreURL != null) {
            driver.navigate().to(restoreURL);
            restoreURL = null;
        }
    }

    /**
     * Switch focus of the driver to the specified frame.
     *
     * @param frame The frame to switch to.
     */
    public void switchToFrame(LogParserFrame frame) {
        switchToMainframe();
        WebElement e = driver.findElement(By.xpath("//frame[@name='" + frame.getFrameName() + "']"));
        driver.switchTo().frame(e);
    }

    /**
     * Switch focus back to the default content.
     */
    public void switchToDefaultContent() {
        driver.switchTo().defaultContent();
    }

    /**
     * Switch focus of the driver to the main frame.
     */
    private void switchToMainframe() {
        WebElement e = driver.findElement(By.xpath("//div[@id='main-panel']//table//tbody//tr//td//iframe"));
        driver.switchTo().frame(e);
    }
}
