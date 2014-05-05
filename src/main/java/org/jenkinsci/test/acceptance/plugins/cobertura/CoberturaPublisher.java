package org.jenkinsci.test.acceptance.plugins.cobertura;

import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.Job;
import org.jenkinsci.test.acceptance.po.PostBuildStepImpl;

/**
 * @author Kohsuke Kawaguchi
 */
@Describable("Publish Cobertura Coverage Report")
public class CoberturaPublisher extends PostBuildStepImpl {
    public CoberturaPublisher(Job parent, String path) {
        super(parent, path);
    }

    public final Control reportFile = control("coberturaReportFile");
}
