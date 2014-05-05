package org.jenkinsci.test.acceptance.plugins.plot;

import org.jenkinsci.test.acceptance.po.*;

/**
 * @author Kohsuke Kawaguchi
 */
@Describable("Plot build data")
public class PlotPublisher extends AbstractStep implements PostBuildStep {
    public final Control group = control("plots/group");
    public final Control title = control("plots/title");

    public PlotPublisher(Job parent, String path) {
        super(parent, path);
    }

    public void source(String type, String path) {
        control("plots/series/fileType[" + type + "]").check();
        control("plots/series/file").set(path);
    }
}
