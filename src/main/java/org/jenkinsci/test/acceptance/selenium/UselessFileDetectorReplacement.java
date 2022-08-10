package org.jenkinsci.test.acceptance.selenium;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.FileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.UselessFileDetector;

/**
 * Replaces the current {@link FileDetector} with the useless one that does nothing. This is handy when you are using a
 * String that is a file that is on a remote system as well as the local one and you do not want Selenium to transfer it
 * across.
 */
public class UselessFileDetectorReplacement implements AutoCloseable {

    private final FileDetector previous;
    private final RemoteWebDriver remoteDriver;

    /** 
     * Create a new instance suitable for use in a try-with-resources block.
     * Example usage is:<code>
     * try (UselessFileDetectorReplacement ufd = new UselessFileDetectorReplacement(driver)) {
     *   control.setText(someTextThatIsAlsoALocalFileName);
     * }
     * </code>
     * @param driver
     */
    public UselessFileDetectorReplacement(WebDriver driver) {
        if (driver instanceof RemoteWebDriver) {
            remoteDriver = (RemoteWebDriver) driver;
            previous = remoteDriver.getFileDetector();
            remoteDriver.setFileDetector(new UselessFileDetector());
        } else {
            remoteDriver = null;
            previous = null;
        }
    }

    @Override
    public void close() {
        if (remoteDriver != null) {
            remoteDriver.setFileDetector(previous);
        }
    }
}