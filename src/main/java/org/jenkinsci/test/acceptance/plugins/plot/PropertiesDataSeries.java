package org.jenkinsci.test.acceptance.plugins.plot;

import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.PageArea;

/**
 * Created by lphex on 4/6/17.
 */
@Describable("properties")
public class PropertiesDataSeries extends DataSeries {
    public PropertiesDataSeries(PageArea area, String path) {
        super(area, path );
        this.selectType();
    }


    public void setLabel(String label){
        control(getPath("label", index())).set(label);
    }

}
