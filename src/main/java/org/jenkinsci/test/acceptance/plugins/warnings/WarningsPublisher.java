package org.jenkinsci.test.acceptance.plugins.warnings;

import org.jenkinsci.test.acceptance.po.*;

/**
 * @author Kohsuke Kawaguchi
 */
@Describable("Scan for compiler warnings")
public class WarningsPublisher extends PostBuildStepImpl {
    private Control addConsoleLogScanner = control("repeatable-add");
    private Control addWorkspaceFileScanner = control("repeatable-add[1]");

    public WarningsPublisher(Job parent, String path) {
        super(parent, path);
    }

    public void addConsoleScanner(String caption) {
        addConsoleLogScanner.click();
        sleep(1000);
        String path = last(by.xpath("//div[@name='consoleParsers']")).getAttribute("path");

        PageArea a = new PageAreaImpl(page, path) {
        };
        a.control("parserName").select(caption);
    }

    public void addWorkspaceFileScanner(String caption, String pattern) {
        addWorkspaceFileScanner.click();
        sleep(1000);
        String path = last(by.xpath("//div[@name='parserConfigurations']")).getAttribute("path");

        PageArea a = new PageAreaImpl(page, path) {
        };
        a.control("pattern").set(pattern);
        a.control("parserName").select(caption);
    }
}
