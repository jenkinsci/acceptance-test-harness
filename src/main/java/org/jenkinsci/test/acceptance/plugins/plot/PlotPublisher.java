package org.jenkinsci.test.acceptance.plugins.plot;

import org.jenkinsci.test.acceptance.po.BuildStepPageObject;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Job;
import org.jenkinsci.test.acceptance.po.PostBuildStep;

/**
 * @author Kohsuke Kawaguchi
 */
@BuildStepPageObject("Plot build data")
public class PlotPublisher extends PostBuildStep {
    public final Control group = control("plots/group");
    public final Control title = control("plots/title");

    public PlotPublisher(Job parent, String path) {
        super(parent, path);
    }

    public void source(String type, String path) {
        control("plots/series/fileType["+type+"]").check();
        control("plots/series/file").set(path);
    }
}
