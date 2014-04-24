package org.jenkinsci.test.acceptance.plugins;

import org.jenkinsci.test.acceptance.po.PostBuildStep;
import org.jenkinsci.test.acceptance.po.ContainerPageObject;
import org.openqa.selenium.WebElement;

import java.net.URL;

public abstract class AbstractCodeStylePluginAction extends ContainerPageObject {

	protected final ContainerPageObject parent;
	
    public AbstractCodeStylePluginAction(ContainerPageObject parent, String plugin) {
        super(parent, parent.url(plugin));
        this.parent = parent;
    }

	public abstract URL getHighPrioUrl();
	
	public int getWarningNumber() {
        return getIntByXPath("//table[@id='summary']/tbody/tr/td[@class='pane'][1]");
    }

    public int getNewWarningNumber() {
        return getIntByXPath("//table[@id='summary']/tbody/tr/td[@class='pane'][2]");
    }

    public int getFixedWarningNumber() {
        return getIntByXPath("//table[@id='summary']/tbody/tr/td[@class='pane'][3]");
    }

    public int getHighWarningNumber() {
        return getIntByXPath("//table[@id='analysis.summary']/tbody/tr/td[@class='pane'][2]");
    }

    public int getNormalWarningNumber() {
        return getIntByXPath("//table[@id='analysis.summary']/tbody/tr/td[@class='pane'][3]");
    }

    public int getLowWarningNumber() {
        return getIntByXPath("//table[@id='analysis.summary']/tbody/tr/td[@class='pane'][4]");
    }
	
	private int getIntByXPath(String xPath) {
		open();
		return asInt(find(by.xpath(xPath)));
	}

    private int asInt(WebElement e) {
        return Integer.parseInt(e.getText().trim());
    }
	
}