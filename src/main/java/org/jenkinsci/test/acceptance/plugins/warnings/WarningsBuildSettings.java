package org.jenkinsci.test.acceptance.plugins.warnings;

import java.util.List;

import org.jenkinsci.test.acceptance.plugins.analysis_core.AnalysisFreestyleSettings;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.Job;
import org.jenkinsci.test.acceptance.po.PageArea;
import org.jenkinsci.test.acceptance.po.PageAreaImpl;
import org.openqa.selenium.WebElement;

/**
 * Settings of the warnings plugin. There is no difference between freestyle and maven jobs.
 */
@Describable("Scan for compiler warnings")
public class WarningsBuildSettings extends AnalysisFreestyleSettings {
    private static final String CONSOLE_PARSERS = "consoleParsers";
    private static final String FILE_PARSERS = "parserConfigurations";

    private Control consoleParsers = findRepeatableAddButtonFor(CONSOLE_PARSERS);
    private Control fileParsers = findRepeatableAddButtonFor(FILE_PARSERS);

    private Control includePattern = control("includePattern");
    private Control excludePattern = control("excludePattern");

    /**
     * Creates a new instance of {@code WarningsBuildSettings}.
     *
     * @param parent the job containing the publisher
     * @param path   the path to the page area
     */
    public WarningsBuildSettings(final Job parent, final String path) {
        super(parent, path);
    }

    public void clearConsoleScanners() {
        removeParsersFor(CONSOLE_PARSERS);
    }

    public void clearWorkspaceScanners() {
        removeParsersFor(FILE_PARSERS);
    }

    private void removeParsersFor(final String propertyName) {
        List<WebElement> deleteButtons = findRepeatableDeleteButtonsFor(propertyName);
        for (WebElement deleteButton : deleteButtons) {
            deleteButton.click();
        }
    }

    public void addConsoleParser(final String parserName) {
        consoleParsers.click();
        elasticSleep(1000);

        PageArea repeatable = getRepeatableAreaOf(CONSOLE_PARSERS);
        repeatable.control("parserName").select(parserName);
    }

    public void addWorkspaceScanner(final String caption, final String pattern) {
        fileParsers.click();
        elasticSleep(1000);

        PageArea repeatable = getRepeatableAreaOf(FILE_PARSERS);
        repeatable.control("pattern").set(pattern);
        repeatable.control("parserName").select(caption);
    }

    private PageArea getRepeatableAreaOf(final String propertyName) {
        String path = last(by.xpath("//div[@name='" + propertyName + "']")).getAttribute("path");

        return new PageAreaImpl(WarningsBuildSettings.this.getPage(), path) {};
    }

    /**
     * Defines the files to remove from all found warnings.
     *
     * @param pattern the pattern of files to exclude from the results
     */
    public void setExcludePattern(final String pattern) {
        ensureAdvancedClicked();
        excludePattern.set(pattern);
    }

    /**
     * Defines the files to include from all found warnings.
     *
     * @param pattern the pattern of files to include from the results
     */
    public void setIncludePattern(final String pattern) {
        ensureAdvancedClicked();
        includePattern.set(pattern);
    }
}
