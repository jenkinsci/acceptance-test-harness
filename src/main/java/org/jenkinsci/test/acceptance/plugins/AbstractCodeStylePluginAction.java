package org.jenkinsci.test.acceptance.plugins;

import org.jenkinsci.test.acceptance.po.ContainerPageObject;
import org.openqa.selenium.WebElement;

import java.net.URL;

/**
 * Abstract action class for plugins with getter of the warnings.
 * @author Martin Kurz
 */
public abstract class AbstractCodeStylePluginAction extends ContainerPageObject {

	protected final ContainerPageObject parent;

    /**
     * Holds the current plugin.
     */
    private final String plugin;

    /**
     * Constructor for a action.
     * @param parent Parent container page object
     * @param plugin Path to plugin without / at the end
     */
    public AbstractCodeStylePluginAction(ContainerPageObject parent, String plugin) {
        super(parent, parent.url(plugin + '/'));
        this.parent = parent;
        this.plugin = plugin;
    }

    /**
     * Getter of the url for high prio Warnings.
     * @return Url for high prio Warnings
     */
	public URL getHighPrioUrl() {
        return parent.url(plugin + "Result/HIGH");
    }

    /**
     * Getter of all warnings.
     * @return Number of warnings
     */
	public int getWarningNumber() {
        return getIntByXPath("//table[@id='summary']/tbody/tr/td[@class='pane'][1]");
    }

    /**
     * Getter of new warnings.
     * @return Number of new warnings
     */
    public int getNewWarningNumber() {
        return getIntByXPath("//table[@id='summary']/tbody/tr/td[@class='pane'][2]");
    }

    /**
     * Getter of fixed warnings.
     * @return Number of fixed warnings
     */
    public int getFixedWarningNumber() {
        return getIntByXPath("//table[@id='summary']/tbody/tr/td[@class='pane'][3]");
    }

    /**
     * Getter of high warnings.
     * @return Number of high warnings
     */
    public int getHighWarningNumber() {
        return getIntByXPath("//table[@id='analysis.summary']/tbody/tr/td[@class='pane'][2]");
    }

    /**
     * Getter of normal warnings.
     * @return Number of normal warnings
     */
    public int getNormalWarningNumber() {
        return getIntByXPath("//table[@id='analysis.summary']/tbody/tr/td[@class='pane'][3]");
    }

    /**
     * Getter of low warnings.
     * @return Number of low warnings.
     */
    public int getLowWarningNumber() {
        return getIntByXPath("//table[@id='analysis.summary']/tbody/tr/td[@class='pane'][4]");
    }

    /**
     * Get number of path.
     * @param xPath Path to go for number to return
     * @return The searched number
     */
	private int getIntByXPath(String xPath) {
		open();
		return asInt(find(by.xpath(xPath)));
	}

    /**
     * Returns the integer value of the webelement.
     * @param e Webelement with number to get
     * @return Integer value of webelement
     */
    protected int asInt(WebElement e) {
        return Integer.parseInt(e.getText().trim());
    }

    /**
     * Returns the trimmed content of the weblement
     * @param webElement the Webelement whose content shall be trimmed
     * @return String the trimmed content
     */
    protected String asTrimmedString(final WebElement webElement) {
        return webElement.getText().trim();
    }
}