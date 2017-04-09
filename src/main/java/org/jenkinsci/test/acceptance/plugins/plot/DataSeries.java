package org.jenkinsci.test.acceptance.plugins.plot;

import gherkin.lexer.Pl;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.PageArea;
import org.jenkinsci.test.acceptance.po.PageAreaImpl;

/**
 * Created by lphex on 4/6/17.
 */
public abstract class  DataSeries  extends PageAreaImpl {

    private Plot parant;

    protected DataSeries(PageArea area, String path) {
        super(area, path);
        parant = (Plot)area;

    }

    public void selectType(){
        String p = "fileType[" + this.getClass().getAnnotation(Describable.class).value()[0] + "]";
        control(p).click();
    }

    public <S extends  DataSeries> S setFileType(Class<S> DataSeriesClass){
        String fileType = DataSeriesClass.getAnnotation(Describable.class).value()[0];
        S series;
        try {
            series =  DataSeriesClass.getConstructor(PageArea.class, String.class)
                    .newInstance(this, fileType);
        }catch (ReflectiveOperationException e) {
            throw new AssertionError("Failed to invoke a constructor of " + DataSeriesClass, e);
        }

        parant.setDataSeries(index(), series);

        series.selectType();

        return  series;
    }

    public int index(){
        return  parant.getSeriesIndex(this);

    }

    public void setFile(String fileName){
        control("file").set(fileName);
    }

    public void deleteSeries(){
        control("repeatable-delete").click();
        parant.removeSeries(this);

    }
}
