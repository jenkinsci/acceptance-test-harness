package org.jenkinsci.test.acceptance.junit;

import jakarta.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.util.logging.Logger;
import org.jenkinsci.test.acceptance.controller.JenkinsController;
import org.jenkinsci.test.acceptance.po.CapybaraPortingLayerImpl;
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

        if (causedBy(t, NoSuchElementException.class)) {
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
            Files.write(
                    diagnostics.touch("screenshot.png").toPath(),
                    ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES));
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
            if (type.isInstance(cur)) {
                return true;
            }
        }
        return false;
    }
}
