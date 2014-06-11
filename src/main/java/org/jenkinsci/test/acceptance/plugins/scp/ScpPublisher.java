package org.jenkinsci.test.acceptance.plugins.scp;

import org.jenkinsci.test.acceptance.po.*;

/**
 * @author Kohsuke Kawaguchi
 */
@Describable("Publish artifacts to SCP Repository")
public class ScpPublisher extends AbstractStep implements PostBuildStep {
    public ScpPublisher(Job parent, String path) {
        super(parent, path);
    }

    public final Control add = control("repeatable-add");

    public Site add() {
        add.click();
        String p = last(by.xpath(".//div[@name='entries'][starts-with(@path,'%s/entries')]", getPath())).getAttribute("path");
        return new Site(getPage(), p);
    }

    public static class Site extends PageAreaImpl {
        public Site(PageObject parent, String path) {
            super(parent, path);
        }

        public final Control sourceFile = control("sourceFile");
        public final Control filePath = control("filePath");
    }
}
