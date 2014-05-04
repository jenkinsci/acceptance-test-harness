package org.jenkinsci.test.acceptance.plugins;

import org.jenkinsci.test.acceptance.po.ContainerPageObject;
import org.openqa.selenium.WebElement;

import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

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

    public SortedMap<String, Integer> getFileTabContents() {
        open();
        find(by.xpath(".//A[@href]/em[text() = 'Files']")).click();
        return getContentsOfVisibleTable(true, true);
    }

    public SortedMap<String, Integer> getCategoriesTabContents() {
        open();
        find(by.xpath(".//A[@href]/em[text() = 'Categories']")).click();
        return getContentsOfVisibleTable(true, true);
    }

    public SortedMap<String, Integer> getTypesTabContents() {
        open();
        find(by.xpath(".//A[@href]/em[text() = 'Types']")).click();
        return getContentsOfVisibleTable(true, true);
    }

    public SortedMap<String, Integer> getWarningsTabContents() {
        open();
        find(by.xpath(".//A[@href]/em[text() = 'Warnings']")).click();
        return getContentsOfVisibleTable(true, false);
    }

    protected SortedMap<String, Integer> getContentsOfVisibleTable(boolean removeHeader, boolean removeFooter) {
        final Collection<WebElement> tables = all(by.xpath("//div[@id='statistics']/div/div/table"));
        for(WebElement table : tables) {
            if(table.isDisplayed()) {
                final List<WebElement> immediateChildRows = table.findElements(by.xpath("./tbody/tr"));

                // remove footer and header
                if(removeHeader) {
                    immediateChildRows.remove(0);
                }

                if(removeFooter) {
                    immediateChildRows.remove(immediateChildRows.size() - 1);
                }

                return mapTableCellsKeyValue(immediateChildRows);
            }
        }

        throw new IllegalStateException("No visible table found");
    }

    protected SortedMap<String, Integer> mapTableCellsKeyValue(final Collection<WebElement> rows) {
        final SortedMap<String, Integer> result = new TreeMap<String, Integer>();
        for(WebElement elem : rows) {
            final List<WebElement> cells = elem.findElements(by.xpath("./td"));
            final String key = asTrimmedString(cells.get(0));
            final Integer value = asInt(cells.get(1));
            result.put(key, value);
        }
        return result;
    }
}