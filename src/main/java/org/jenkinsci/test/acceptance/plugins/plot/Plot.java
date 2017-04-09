package org.jenkinsci.test.acceptance.plugins.plot;

import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.PageArea;
import org.jenkinsci.test.acceptance.po.PageAreaImpl;

import javax.xml.crypto.Data;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lphex on 4/6/17.
 */
public class Plot extends PageAreaImpl{

    private PlotPublisher parant;
    private List<DataSeries> series;


    protected Plot(PageArea area, String path) {
        super(area, path);
        parant =  (PlotPublisher) area;
        series = new ArrayList<>();
    }

    /**
     * Own index in ArrayList of Parent PageArea
     * @return
     */
    private int index(){
        return parant.getPlotIndex(this);
    }


    /**
     * Create a DataSeries of selected Type and select the Control. If not the first DataSeries create
     * a new onw with Add Button.
     * @param series
     * @param <S>
     * @return
     */
    public <S extends DataSeries> S addDataSeries(Class<S> series){
        if (this.series.size()>1){
            control("repeatable-add").click();
            //todo insert wait period to make sure the new PageArea appers
        }

        S s;
        String fileType = series.getAnnotation(Describable.class).value()[0];
        try {
            s = series.getConstructor(PageArea.class, String.class)
                    .newInstance(this, getPath("series", this.series.size()));
        }catch (ReflectiveOperationException e) {
            throw new AssertionError("Failed to invoke a constructor of " + series, e);
        }

        this.series.add(s);
        return s;
    }

    /**
     * Index used for construction of path
     * @param dataSeries
     * @return
     */
    protected int getSeriesIndex(DataSeries dataSeries){
        return series.indexOf(dataSeries);
    }

    protected void removeSeries(DataSeries dataSeries){
        series.remove(dataSeries);

    }

    protected void setDataSeries(int index, DataSeries s){
        this.series.add(index, s);
    }

    public DataSeries getSeries(int index){
        return  series.get(index -1);
    }

    public void setGroup(String group){
        control("group").set(group);
    }


    public void setTitle(String title){
        control("title").set(title);
    }


    public void setNumBuilds(String numBuilds){
        control("numBuilds").set(numBuilds);
    }


    public void setYaxisLabel(String yAxisLabel){
        control("yaxis").set(yAxisLabel);
    }


    public void setUseDescr(String descr){
        control ( "useDescr").set(descr);
    }


    public Control getExclZero(){
        return control ("exclZero");
    }


    public Control getLogarithmic(){
        return control( "logarithmic");
    }


    public Control getKeepRecords(){
        return control("keepRecords");
    }

}
