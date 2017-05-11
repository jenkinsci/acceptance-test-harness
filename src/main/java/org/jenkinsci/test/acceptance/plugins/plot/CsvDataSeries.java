package org.jenkinsci.test.acceptance.plugins.plot;

import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.PageArea;

/**
 * @author Stefan Schuhbaeck, Lukasz Koceluch
 */
@Describable("csv")
public class CsvDataSeries extends DataSeries {

    public CsvDataSeries(PageArea area, String path) {
        super(area, path);
        this.selectType();
    }

    public void setUrl(String url) {
        control("url").set(url);
    }

    public  void setExclusionValues(String exclusionValues) {
        control(getFileType() + "/exclusionValues").set(exclusionValues);
    }

    public void checkTabel() {
        control(getFileType() + "/displayTableFlag").check();
    }

    public void selectIncludeAllColumns() {
        control(getFileType() +"/inclusionFlag[OFF]").click();
    }

    public void selectIncludeByName() {
        control(getFileType() +"/inclusionFlag[INCLUDE_BY_STRING]").click();
    }

    public void selectExcludeByName() {
        control(getFileType() +"/inclusionFlag[EXCLUDE_BY_STRING]").click();
    }

    public void selectIncludeByIndex() {
        control(getFileType() +"/inclusionFlag[INCLUDE_BY_COLUMN]").click();
    }

    public void selectExcludeByIndex() {
        control(getFileType() +"/inclusionFlag[EXCLUDE_BY_COLUMN]").click();
    }


}
