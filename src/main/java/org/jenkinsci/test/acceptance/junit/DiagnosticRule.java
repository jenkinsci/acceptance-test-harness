package org.jenkinsci.test.acceptance.junit;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;
import javax.inject.Inject;
import org.apache.commons.io.FileUtils;
import org.jenkinsci.test.acceptance.controller.JenkinsController;
import org.jenkinsci.test.acceptance.po.CapybaraPortingLayerImpl;
import org.jenkinsci.test.acceptance.selenium.SanityChecker;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

@GlobalRule
public class DiagnosticRule extends TestWatcher {
    private static final Logger logger = Logger.getLogger(DiagnosticRule.class.getName());

    @Inject
    FailureDiagnostics diagnostics;
    @Inject
    JenkinsController controller;
    @Inject
    WebDriver driver;

    @Override
    protected void failed(Throwable t, Description description) {
        takeScreenshot();

        if (causedBy(t, NoSuchElementException.class) || causedBy(t, SanityChecker.Failure.class)) {
            writeHtmlPage();
        }

        try {
            controller.diagnose(t);
        } catch (IOException e) {
            throw new Error(e);
        }

    }

    private void takeScreenshot() {
        try {
            File file = diagnostics.touch("screenshot.png");
            File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            FileUtils.copyFile(screenshot, file);

        } catch (IOException e) {
            logger.warning("An error occurred when taking screenshot");
            throw new Error(e);
        }
    }

    private void writeHtmlPage() {
        diagnostics.write("last-page.html", CapybaraPortingLayerImpl.getPageSource(driver));
    }

    /**
     * Detect the outermost exception of given type.
     */
    private boolean causedBy(Throwable caught, Class<? extends Throwable> type) {
        for (Throwable cur = caught; cur != null; cur = cur.getCause()) {
            if (type.isInstance(cur))
                return true;
        }
        return false;
    }

}
