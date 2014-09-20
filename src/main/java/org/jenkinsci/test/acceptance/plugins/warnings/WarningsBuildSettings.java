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
    private Control addConsoleLogScanner = control("repeatable-add");
    private Control addWorkspaceFileScanner = control("repeatable-add[1]");

    public WarningsBuildSettings(Job parent, String path) {
        super(parent, path);
    }

    public void addConsoleScanner(String caption) {
        addConsoleLogScanner.click();
        sleep(1000);
        String path = last(by.xpath("//div[@name='consoleParsers']")).getAttribute("path");

        PageArea a = new PageAreaImpl(getPage(), path) {
        };
        a.control("parserName").select(caption);
    }

    public void addWorkspaceFileScanner(String caption, String pattern) {
        addWorkspaceFileScanner.click();
        sleep(1000);
        String path = last(by.xpath("//div[@name='parserConfigurations']")).getAttribute("path");

        PageArea a = new PageAreaImpl(getPage(), path) {
        };
        a.control("pattern").set(pattern);
        a.control("parserName").select(caption);
    }

    public void addWarningsToInclude(String pattern){
        ensureAdvancedClicked();

        find(by.xpath("//input[@name='_.includePattern']")).sendKeys(pattern);
    }


    public void addWarningsToIgnore(String pattern){
        ensureAdvancedClicked();

        find(by.xpath("//input[@name='_.excludePattern']")).sendKeys(pattern);
    }
}
