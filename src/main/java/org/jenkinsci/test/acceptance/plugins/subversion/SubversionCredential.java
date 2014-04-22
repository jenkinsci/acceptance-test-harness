package org.jenkinsci.test.acceptance.plugins.subversion;

import com.google.inject.Injector;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.PageObject;
import org.openqa.selenium.JavascriptExecutor;

import java.net.URL;
import java.util.Set;

/**
 * Superclass for the different authentication methods in the SVN credential page.
 *
 * @author Matthias Karl
 */
public class SubversionCredential extends PageObject {
    public static final String BUTTON_OK = "OK";

    private static final int EXPECTED_AMOUNT_OF_HANDLES = 2;

    private String parentWindowHandle = null;
    private String windowHandle = null;


    public SubversionCredential(Injector injector, URL url, String parentWindowHandle) throws Exception {
        super(injector, url);
        //save the parent windowHandle
        this.parentWindowHandle = parentWindowHandle;
        //set the windowHandle of the popup window
        this.windowHandle = initiateWindowHandle(parentWindowHandle);
    }

    /**
     * Tries to find the windowHandle of the popup window in the handle-list of the driver.
     *
     * @param parentWindowHandle p
     * @return Key-String of the handle
     * @throws SubversionPluginTestException if more than two Handles are found at the driver
     */
    private String initiateWindowHandle(String parentWindowHandle) throws SubversionPluginTestException {
        final Set<String> windowHandles = driver.getWindowHandles();
        String ownHandle = null;
        if (windowHandles.size() > EXPECTED_AMOUNT_OF_HANDLES) {
            SubversionPluginTestException.throwCouldNotDeterminePopupWindow();
        } else {
            for (String windowHandle : windowHandles) {
                if (!windowHandle.equals(parentWindowHandle)) {
                    ownHandle = windowHandle;
                }
            }
        }
        return ownHandle;
    }


    /**
     * Confirms and closes the Credential dialog.
     */
    public void confirmDialog() {
        switchToPopupHandle();
        Control buttonOk = control(by.button(BUTTON_OK));
        buttonOk.click();

        ((JavascriptExecutor) driver).executeScript("window.close()");

        switchToParentHandle();
    }

    protected void switchToParentHandle() {
        switchHandle(getWindowHandle());
    }

    protected void switchToPopupHandle() {
        switchHandle(getParentWindowHandle());
    }

    private void switchHandle(String handle) {
        driver.switchTo().window(handle);
    }

    public String getParentWindowHandle() {
        return parentWindowHandle;
    }

    public String getWindowHandle() {
        return windowHandle;
    }
}
