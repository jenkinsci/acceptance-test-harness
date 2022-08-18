package org.jenkinsci.test.acceptance.selenium;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WrapsDriver;
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
     * Example usage is:
     * <pre>
     * {@code
     * try (UselessFileDetectorReplacement ufd = new UselessFileDetectorReplacement(driver)) {
     *   control.setText(someTextThatIsAlsoALocalFileName);
     * }
     * }
     * </pre>
     */
    public UselessFileDetectorReplacement(WebDriver driver) {
        driver = getNonWrappedDriver(driver);
        if (driver.getClass().equals(RemoteWebDriver.class)) {
            // we test the explicit class not instanceof as the local FirefoxDriver and others 
            // are also RemoteWebDriver but can not be configured with a FileDetector
            remoteDriver = (RemoteWebDriver) driver;
            previous = remoteDriver.getFileDetector();
            remoteDriver.setFileDetector(new UselessFileDetector());
        } else {
            remoteDriver = null;
            previous = null;
        }
    }

    /**
     * Obtain the underlying driver if the driver is {@link WrapsDriver wrapped} otherwise returns {@code driver}. 
     * @param driver the {@link WebDriver} to unwrap or return if it is not wrapped. 
     */
    private WebDriver getNonWrappedDriver(WebDriver driver) {
        WebDriver d = driver;
        while (d instanceof WrapsDriver) {
            d = ((WrapsDriver)d).getWrappedDriver();
        }
        return d;
    }

    @Override
    public void close() {
        if (remoteDriver != null) {
            remoteDriver.setFileDetector(previous);
        }
    }
}
