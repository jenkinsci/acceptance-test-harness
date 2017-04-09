package org.jenkinsci.test.acceptance.plugins.plot;

import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.PageArea;

/**
 * Created by lphex on 4/6/17.
 */
@Describable("xml")
public class xmlDataSeries extends DataSeries {
    public xmlDataSeries(PageArea area, String path) {
        super(area, "fileType[" + path + "]");
        this.selectType();
    }

    public void setXpath(String xpath){
        control(getPath("xpath", index())).set(xpath);
    }

    public void setUrl(String url){
        control(getPath("url", index())).set(url);
    }

    //todo radio buttons

}
