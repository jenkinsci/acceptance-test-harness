package org.jenkinsci.test.acceptance.plugins.subversion;

import com.google.inject.Injector;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.PageObject;
import org.openqa.selenium.JavascriptExecutor;

import java.net.URL;
import java.util.Set;

/**
 * Created by karl on 4/15/14.
 */
public class SubversionCredential extends PageObject {
    public static final String BUTTON_OK = "OK";
    public static final String BUTTON_CLOSE = "Close";

    private String parentWindowHandle = null;
    private String windowHandle = null;


    public SubversionCredential(Injector injector, URL url, String parentWindowHandle) throws Exception {
        super(injector, url);
        this.parentWindowHandle = parentWindowHandle;
        this.windowHandle = initiateWindowHandle(parentWindowHandle);
    }

    private String initiateWindowHandle(String parentWindowHandle) throws Exception {
        final Set<String> windowHandles = driver.getWindowHandles();
        String ownHandle = null;
        if (windowHandles.size() > 2) {
            //TODO: Eigene Exception einführen
            throw new Exception();
        } else {
            for (String windowHandle : windowHandles) {
                if (!windowHandle.equals(parentWindowHandle)) {
                    ownHandle = windowHandle;
                }
            }
        }
        return ownHandle;
    }

    public String getParentWindowHandle() {
        return parentWindowHandle;
    }

    public String getWindowHandle() {
        return windowHandle;
    }

    public void confirmDialog() throws Exception {
        switchHandle(windowHandle);
        Control buttonOk = control(by.button(BUTTON_OK));
        buttonOk.click();

        ((JavascriptExecutor)driver).executeScript("window.close()");

        switchHandle(parentWindowHandle);
    }

    protected void switchHandle(String handle) {
        driver.switchTo().window(handle);
    }
}
