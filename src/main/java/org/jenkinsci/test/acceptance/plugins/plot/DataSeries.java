package org.jenkinsci.test.acceptance.plugins.plot;

import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.PageArea;
import org.jenkinsci.test.acceptance.po.PageAreaImpl;

/**
 * @author Stefan Schuhbaeck, Lukasz Koceluch
 */
public abstract class  DataSeries  extends PageAreaImpl {

    protected Plot area;
    protected String path;

    protected DataSeries(PageArea area, String path) {
        super(area, path);
        this.area = (Plot)area;
        this.path = path;
    }

    protected String getFileType() {
        return "fileType[" + this.getClass().getAnnotation(Describable.class).value()[0] + "]";
    }

    public void selectType() {
        control(getFileType()).click();
    }

    public abstract DataSeries setFileType();

    public int index() {
        return  area.getSeriesIndex(this);

    }

    public void setFile(String fileName) {
        control("file").set(fileName);
    }

    public void deleteSeries() {
        control("repeatable-delete").click();
        area.removeSeries(this);

    }
}
