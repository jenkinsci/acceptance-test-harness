package org.jenkinsci.test.acceptance.plugins.xunit;

import org.jenkinsci.test.acceptance.po.AbstractStep;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.Job;
import org.jenkinsci.test.acceptance.po.PageAreaImpl;
import org.jenkinsci.test.acceptance.po.PageObject;
import org.jenkinsci.test.acceptance.po.PostBuildStep;

/**
 * @author Kohsuke Kawaguchi
 */
@Describable("Publish xUnit test result report")
public class XUnitPublisher extends AbstractStep implements PostBuildStep {
    public final Control addButton = control("hetero-list-add[tools]");

    public XUnitPublisher(Job parent, String path) {
        super(parent, path);
    }

    public Tool addTool(final String kind) {
        String path = createPageArea("tools", () -> addButton.selectDropdownMenu(kind));
        return newInstance(Tool.class, getPage(), path);
    }

    public static class Tool extends PageAreaImpl {
        public final Control pattern = control("pattern");

        public Tool(PageObject parent, String path) {
            super(parent, path);
        }
    }
}
