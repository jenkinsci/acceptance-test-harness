package org.jenkinsci.test.acceptance.plugins.xunit;

import org.jenkinsci.test.acceptance.po.*;

/**
 * @author Kohsuke Kawaguchi
 */
@Describable("Publish xUnit test result report")
public class XUnitPublisher extends AbstractStep implements PostBuildStep {
    public final Control addButton = control("hetero-list-add[tools]");

    public XUnitPublisher(Job parent, String path) {
        super(parent, path);
    }

    public Tool addTool(String kind) {
        addButton.selectDropdownMenu(kind);
        String path = last(by.xpath("//div[starts-with(@path, '%s/tools')]",super.getPath())).getAttribute("path");
        return newInstance(Tool.class, getPage(), path);
    }

    public static class Tool extends PageAreaImpl {
        public final Control pattern = control("pattern");

        public Tool(PageObject parent, String path) {
            super(parent, path);
        }
    }
}
