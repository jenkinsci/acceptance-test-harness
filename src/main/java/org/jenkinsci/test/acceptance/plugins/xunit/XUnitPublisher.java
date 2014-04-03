package org.jenkinsci.test.acceptance.plugins.xunit;

import com.google.inject.Injector;
import org.jenkinsci.test.acceptance.po.*;

/**
 * @author Kohsuke Kawaguchi
 */
@BuildStepPageObject("Publish xUnit test result report")
public class XUnitPublisher extends PostBuildStep {
    public final Control addButton = control("hetero-list-add[tools]");

    public XUnitPublisher(Job parent, String path) {
        super(parent, path);
    }

    public Tool addTool(String kind) {
        selectDropdownMenu(kind,addButton.resolve());

        String path = last(by.xpath("//div[starts-with(@path, '%s/tools')]",super.path)).getAttribute("path");

        return newInstance(Tool.class, injector, path);
    }

    public static class Tool extends PageArea {
        public final Control pattern = control("pattern");

        public Tool(Injector injector, String path) {
            super(injector, path);
        }
    }
}
