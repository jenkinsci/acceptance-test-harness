package org.jenkinsci.test.acceptance.plugins.scp;

import org.jenkinsci.test.acceptance.po.*;

/**
 * @author Kohsuke Kawaguchi
 */
@Describable("Publish artifacts to SCP Repository")
public class ScpPublisher extends PostBuildStep {
    public ScpPublisher(Job parent, String path) {
        super(parent, path);
    }

    public final Control add = control("repeatable-add");

    public Site add() {
        add.click();
        String p = last(by.xpath(".//div[@name='entries'][starts-with(@path,'%s/entries')]", path)).getAttribute("path");
        return new Site(page, p);
    }

    public static class Site extends PageAreaImpl {
        public Site(PageObject parent, String path) {
            super(parent, path);
        }

        public final Control sourceFile = control("sourceFile");
        public final Control filePath = control("filePath");
    }
}
