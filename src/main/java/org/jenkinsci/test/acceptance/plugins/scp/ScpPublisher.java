package org.jenkinsci.test.acceptance.plugins.scp;

import com.google.inject.Injector;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.Job;
import org.jenkinsci.test.acceptance.po.PageArea;
import org.jenkinsci.test.acceptance.po.PostBuildStep;

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
        return new Site(injector,p);
    }

    public static class Site extends PageArea {
        public Site(Injector injector, String path) {
            super(injector, path);
        }

        public final Control sourceFile = control("sourceFile");
        public final Control filePath = control("filePath");
    }
}
