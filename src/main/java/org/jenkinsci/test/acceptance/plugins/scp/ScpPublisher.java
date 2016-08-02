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
        String path = createPageArea("entries", new Runnable() {
            @Override public void run() {
                add.click();
            }
        });
        return new Site(getPage(), path);
    }

    public static class Site extends PageAreaImpl {
        public Site(PageObject parent, String path) {
            super(parent, path);
        }

        public final Control sourceFile = control("sourceFile");
        public final Control filePath = control("filePath");
    }
}
