package org.jenkinsci.test.acceptance.plugins.warnings;

import org.jenkinsci.test.acceptance.po.*;

/**
 * @author Kohsuke Kawaguchi
 */
@Describable("Scan for compiler warnings")
public class WarningsPublisher extends AbstractStep implements PostBuildStep {
    private Control addConsoleLogScanner = control("repeatable-add");
    private Control addWorkspaceFileScanner = control("repeatable-add[1]");

    public WarningsPublisher(Job parent, String path) {
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

    public void openAdvancedOptions(){
        clickButton("Advanced...", 5);
    }

    public void addWarningsToInclude(String pattern){
        find(by.xpath("//input[@name='_.includePattern']")).sendKeys(pattern);
    }


    public void addWarningsToIgnore(String pattern){
        find(by.xpath("//input[@name='_.excludePattern']")).sendKeys(pattern);
    }


    public void runAlways(){
        check(find(by.xpath("//input[@name='canRunOnFailed']")));
    }


    public void detectModules(){
        check(find(by.xpath("//input[@name='shouldDetectModules']")));
    }


    public void resolveRelativePaths(){
        check(find(by.xpath("//input[@name='shouldDetectModules']")));
    }
}
