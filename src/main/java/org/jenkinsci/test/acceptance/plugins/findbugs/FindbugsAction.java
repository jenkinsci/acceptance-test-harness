package org.jenkinsci.test.acceptance.plugins.findbugs;

import org.jenkinsci.test.acceptance.po.ContainerPageObject;
import org.openqa.selenium.WebElement;

import java.net.URL;

/**
 * Page object for Findbugs action.
 */
public class FindbugsAction extends ContainerPageObject {
    private final ContainerPageObject parent;

    public FindbugsAction(ContainerPageObject parent) {// Build or Job
        super(parent, parent.url("findbugs/"));
        this.parent = parent;
    }

    public URL getHighPrioUrl() {
        return parent.url("findbugsResult/HIGH");
    }

    public int getWarningNumber() {
        open();
        return asInt(find(by.xpath("//table[@id='summary']/tbody/tr/td[@class='pane'][1]")));
    }

    public int getNewWarningNumber() {
        open();
        return asInt(find(by.xpath("//table[@id='summary']/tbody/tr/td[@class='pane'][2]")));
    }

    public int getFixedWarningNumber() {
        open();
        return asInt(find(by.xpath("//table[@id='summary']/tbody/tr/td[@class='pane'][3]")));
    }

    public int getHighWarningNumber() {
        open();
        return asInt(find(by.xpath("//table[@id='analysis.summary']/tbody/tr/td[@class='pane'][2]")));
    }

    public int getNormalWarningNumber() {
        open();
        return asInt(find(by.xpath("//table[@id='analysis.summary']/tbody/tr/td[@class='pane'][3]")));
    }

    public int getLowWarningNumber() {
        open();
        return asInt(find(by.xpath("//table[@id='analysis.summary']/tbody/tr/td[@class='pane'][4]")));
    }

    private int asInt(WebElement e) {
        return Integer.parseInt(e.getText().trim());
    }
}
