package org.jenkinsci.test.acceptance.plugins.plot;

import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.PageArea;

/**
 * @author Stefan Schuhbaeck, Lukasz Koceluch
 */
@Describable("xml")
public class XmlDataSeries extends DataSeries {
    public XmlDataSeries(PageArea area, String path) {
        super(area,  path );
        this.selectType();
    }

    public void setXpath(String xpath){
        control(getFileType() + "/xpath").set(xpath);
    }

    public void setUrl(String url){
        control(getFileType() +"/url").set(url);
    }

    public void selectResultTypNodeset(){
        control(getFileType() +"/nodeType[NODESET]").click();
    }

    public void selectResultTypNode(){
        control(getFileType() +"/nodeType[NODE]").click();
    }

    public void selectResultTypString(){
        control(getFileType() +"/nodeType[STRING]").click();
    }

    public void selectResultTypBoolean(){
        control(getFileType() +"/nodeType[BOOLEAN]").click();
    }

    public void selectResultTypNumber(){
        control(getFileType() +"/nodeType[NUMBER]").click();
    }


}
