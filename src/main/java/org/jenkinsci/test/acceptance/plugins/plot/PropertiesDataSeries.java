package org.jenkinsci.test.acceptance.plugins.plot;

import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.PageArea;

/**
 * @author Stefan Schuhbaeck, Lukasz Koceluch
 */
@Describable("properties")
public class PropertiesDataSeries extends DataSeries {
    public PropertiesDataSeries(PageArea area, String path) {
        super(area, path );
        this.selectType();
    }

    public void setLabel(String label){
        control(getFileType() + "/label").set(label);
    }

}
