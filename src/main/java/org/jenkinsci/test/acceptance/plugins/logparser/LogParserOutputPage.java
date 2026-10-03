package org.jenkinsci.test.acceptance.plugins.logparser;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jenkinsci.test.acceptance.po.PageObject;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

/**
 * The logparser plugin uses two frames.
 * <p>
 * To check the content of the frames the selenium driver must be switched to the frames.
 *
 * @author Michael Engel.
 */
public class LogParserOutputPage extends PageObject {

    /**
     * Defines the different types of frames used by the parsed output of the logparser
     */
    public enum LogParserFrame {
        SIDEBAR("sidebar"),
        CONTENT("content");

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
        super(po, po.url("parsed_console"));
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

    /**
     * Get the fragment part of the URL of the content frame after a link in the sidebar was clicked.
     * @param category The parser category of the link.
     * @param index The index of the link in the category, starting with 1.
     * @return The fragment part of the URL.
     * @throws Exception There is no such link.
     */
    public String getFragmentOfContentFrame(String category, int index) throws Exception {
        try {
            switchToFrame(LogParserFrame.SIDEBAR);
            driver.findElement(By.partialLinkText(category)).click();
            driver.findElement(By.xpath("//ul[@id='" + category + "']/li[" + index + "]/a"))
                    .click();
            switchToDefaultContent();
            switchToFrame(LogParserFrame.CONTENT);
            String[] parts = getCurrentUrlWithFragment().split("#");
            return parts[parts.length - 1];
        } finally {
            switchToDefaultContent();
        }
    }

    /**
     * Get the color of the text identified by logparser.
     * @param category The category of the logparser.
     * @param index The index of matches for this category, starting by 1.
     * @return The color of the text.
     * @throws Exception The text couldn't be found.
     */
    public String getColor(String category, int index) throws Exception {
        try {
            switchToFrame(LogParserFrame.CONTENT);
            WebElement span;
            try {
                String xpath = "//a[@name='" + category.toUpperCase() + index + "']/following-sibling::span[1]";
                span = find(by.xpath(xpath));
            } catch (NoSuchElementException ex) {
                String xpath =
                        "//a[@name='" + category.toUpperCase() + index + "']/parent::p/following-sibling::span[1]";
                span = find(by.xpath(xpath));
            }
            Pattern colorPattern = Pattern.compile("color: (\\S*);");
            Matcher match = colorPattern.matcher(span.getAttribute("style"));
            if (match.find()) {
                return match.group(1);
            } else {
                return "";
            }
        } finally {
            switchToDefaultContent();
        }
    }
}
