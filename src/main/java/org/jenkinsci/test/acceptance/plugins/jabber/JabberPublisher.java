package org.jenkinsci.test.acceptance.plugins.jabber;

import org.jenkinsci.test.acceptance.po.AbstractStep;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.Job;
import org.jenkinsci.test.acceptance.po.PageAreaImpl;
import org.jenkinsci.test.acceptance.po.PageObject;
import org.jenkinsci.test.acceptance.po.PostBuildStep;

/**
 * @author jenky-hm
 */
@Describable("Jabber Notification")
public class JabberPublisher extends AbstractStep implements PostBuildStep {

    public JabberPublisher(Job parent, String path) {
        super(parent, path);
    }

    public Publishers setPublisher() {
        String p = last(by.xpath(".//div[@name='publisher'][starts-with(@path,'%s')]", getPath()))
                .getAttribute("path");
        return new Publishers(getPage(), p);
    }

    public class Publishers extends PageAreaImpl {
        public Publishers(PageObject parent, String path) {
            super(parent, path);
        }

        public final Control targets = control("targets");
    }
}
