package org.jenkinsci.test.acceptance.plugins.plot;

import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.PageArea;

/**
 * Created by lphex on 4/6/17.
 */
@Describable("csv")
public class CsvDataSeries extends DataSeries {
    public CsvDataSeries(PageArea area, String path) {
        super(area,  path );
        this.selectType();
    }


    public void setUrl(String url){
        control("url").set(url);
    }

    public  void setExclusionValues(String exclusionValues){
        control("exclusionValues").set(exclusionValues);
    }

    //todo radio buttons
}
