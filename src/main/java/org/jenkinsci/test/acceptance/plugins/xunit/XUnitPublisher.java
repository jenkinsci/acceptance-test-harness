package org.jenkinsci.test.acceptance.plugins.xunit;

import org.jenkinsci.test.acceptance.po.*;

/**
 * @author Kohsuke Kawaguchi
 */
@Describable("Publish xUnit test result report")
public class XUnitPublisher extends PostBuildStep {
    public final Control addButton = control("hetero-list-add[tools]");

    public XUnitPublisher(Job parent, String path) {
        super(parent, path);
    }

    public Tool addTool(String kind) {
        selectDropdownMenu(kind, addButton.resolve());

        String path = last(by.xpath("//div[starts-with(@path, '%s/tools')]", super.path)).getAttribute("path");

        return newInstance(Tool.class, page, path);
    }

    public static class Tool extends PageAreaImpl {
        public final Control pattern = control("pattern");

        public Tool(PageObject parent, String path) {
            super(parent, path);
        }
    }
}
