package org.jenkinsci.test.acceptance.plugins.cobertura;

import org.jenkinsci.test.acceptance.po.*;

/**
 * @author Kohsuke Kawaguchi
 */
@Describable("Publish Cobertura Coverage Report")
public class CoberturaPublisher extends AbstractStep implements PostBuildStep {
    public CoberturaPublisher(Job parent, String path) {
        super(parent, path);
    }

    public final Control reportFile = control("coberturaReportFile");
}
