package org.jenkinsci.test.acceptance.plugins.warnings;

import org.jenkinsci.test.acceptance.plugins.analysis_core.AnalysisFreestyleSettings;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.Job;
import org.jenkinsci.test.acceptance.po.PageArea;
import org.jenkinsci.test.acceptance.po.PageAreaImpl;

/**
 * Settings of the warnigns plugin. There is no difference between freestyle and maven jobs.
 */
@Describable("Scan for compiler warnings")
public class WarningsBuildSettings extends AnalysisFreestyleSettings {
    private static final String CONSOLE_PARSERS = "consoleParsers";
    private static final String FILE_PARSERS = "parserConfigurations";

    private Control consoleParsers = repeatableAddButton(CONSOLE_PARSERS);
    private Control fileParsers = repeatableAddButton(FILE_PARSERS);

    /**
     * Creates a new instance of {@code WarningsBuildSettings}.
     *
     * @param parent the job containing the publisher
     * @param path   the path to the page area
     */
    public WarningsBuildSettings(final Job parent, final String path) {
        super(parent, path);
    }

    public void addConsoleScanner(final String caption) {
        consoleParsers.click();
        elasticSleep(1000);
        PageArea repeatable = getRepeatableAreaOf(CONSOLE_PARSERS);
        repeatable.control("parserName").select(caption);
    }

    public void addWorkspaceFileScanner(final String caption, final String pattern) {
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

    public void addWarningsToInclude(final String value) {
        setPattern("include", value);
    }

    public void addWarningsToIgnore(final String value) {
        setPattern("exclude", value);
    }

    private void setPattern(final String name, final String value) {
        ensureAdvancedClicked();

        find(by.xpath("//input[@name='_." + name + "Pattern']")).sendKeys(value);
    }
}
