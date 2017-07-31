package org.jenkinsci.test.acceptance.plugins.plot;

import org.jenkinsci.test.acceptance.po.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Kohsuke Kawaguchi
 */
@Describable("Plot build data")
public class PlotPublisher extends AbstractStep implements PostBuildStep {

    private List<Plot> plots;


    public PlotPublisher(Job parent, String path) {

        super(parent, path);

        this.plots = new ArrayList<>();
        Plot p = new Plot(this, getPath("plots", plots.size()));
        plots.add(p);

    }

    public Plot getPlot(int index){
        return plots.get(index -1);
    }


    public Plot addPlot(){
        if (plots.size() >= 1) {
            control("repeatable-add").click();
        }
        Plot p =  new Plot(this, getPath("plots", plots.size()));
        plots.add(p);
        return  p;
    }


    protected int getPlotIndex(Plot p) {
        return plots.indexOf(p);
    }
}
