package org.jenkinsci.test.acceptance.plugins.logparser;

import org.jenkinsci.test.acceptance.po.PageObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

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
     * Get the number of matches identified by the logparser for one category.
     * @param category The name of the category.
     * @return The number of matches.
     * @throws Exception Number not found or number not correctly extracted.
     */
    public int getNumberOfMatches(String category) throws Exception {
        try {
            switchToFrame(LogParserFrame.SIDEBAR);
            WebElement link = driver.findElement(By.partialLinkText(category));
            // text looks like "Error (5)"
            String text = link.getText();
            text = text.substring(category.length() + 2, text.indexOf(")"));
            return Integer.parseInt(text);
        } finally {
            switchToDefaultContent();
        }
    }

    /**
     * Get a list of all links for one category.
     * @param category The name of the category.
     * @return The list elements belonging to this category.
     * @throws Exception The list elements are not found.
     */
    public List<WebElement> getLinkList(String category) throws Exception {
        try {
            switchToFrame(LogParserFrame.SIDEBAR);
            return driver.findElements(By.xpath("//ul[@id='" + category + "']/li"));
        } finally {
            switchToDefaultContent();
        }
    }
}
