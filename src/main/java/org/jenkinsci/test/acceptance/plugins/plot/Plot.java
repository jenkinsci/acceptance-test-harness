package org.jenkinsci.test.acceptance.plugins.plot;

import org.jenkinsci.test.acceptance.po.PageArea;
import org.jenkinsci.test.acceptance.po.PageAreaImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Stefan Schuhbaeck, Lukasz Koceluch
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
     * @return the plot index
     */
    private int index(){
        return parant.getPlotIndex(this);
    }


    /**
     * Create a DataSeries of selected Type and select the Control. If not the first DataSeries create
     * a new one with Add Button.
     * @param seriesClass the series class type
     * @param <S> series class name as generic
     * @return series
     */
    public <S extends DataSeries> S addDataSeries(Class<S> seriesClass){
        if (this.series.size()>=1){
            control("repeatable-add").click();
            //todo insert wait period to make sure the new PageArea appers
        }

        S series;
        try {
            series = seriesClass.getConstructor(PageArea.class, String.class)
                    .newInstance(this, getPath("series", this.series.size()));
        }catch (ReflectiveOperationException e) {
            throw new AssertionError("Failed to invoke a constructor of " + seriesClass, e);
        }

        this.series.add(series);
        return series;
    }

    public <S extends DataSeries> S addDataSeries(Class<S> seriesClass, String file) {
        S series = addDataSeries(seriesClass);
        series.setFile(file);
        return series;
    }

    /**
     * Index used for construction of path
     * @param dataSeries the data series object
     * @return series index
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

    public void setStyle(String plotStyle){
        control("style").select(plotStyle);
    }

    public void setUseDescr(String descr){
        control ( "useDescr").set(descr);
    }

    public void checkExclZero(){
        control ("exclZero").check();
    }

    public void checkLogarithmic(){
        control( "logarithmic").check();
    }

    public void checkKeepRecords(){
        control("keepRecords").check();
    }

}
