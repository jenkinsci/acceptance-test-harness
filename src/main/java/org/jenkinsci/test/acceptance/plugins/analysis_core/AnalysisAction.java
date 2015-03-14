package org.jenkinsci.test.acceptance.plugins.analysis_core;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.test.acceptance.po.ContainerPageObject;
import org.openqa.selenium.WebElement;

/**
 * Abstract action class for static analysis plugins with getters for warnings.
 *
 * @author Martin Kurz
 */
public abstract class AnalysisAction extends ContainerPageObject {
    protected final ContainerPageObject parent;

    /**
     * Holds the current plugin.
     */
    private final String plugin;

    /**
     * Constructor for a action.
     *
     * @param parent Parent container page object
     * @param plugin Path to plugin without / at the end
     */
    public AnalysisAction(ContainerPageObject parent, String plugin) {
        super(parent, parent.url(plugin + '/'));
        this.parent = parent;
        this.plugin = plugin;
    }

    /**
     * Getter of the url for high prio Warnings.
     *
     * @return Url for high prio Warnings
     */
    public URL getHighPrioUrl() {
        return parent.url(plugin + "Result/HIGH");
    }

    /**
     * Getter of the relative path for new Warnings page.
     *
     * @return String for new Warnings page
     */
    public String getNewWarningsUrlAsRelativePath() {
        return plugin + "Result/new";
    }

    /**
     * Getter of all warnings.
     *
     * @return Number of warnings
     */
    public int getWarningNumber() {
        return getIntByXPath("//table[@id='summary']/tbody/tr/td[@class='pane'][1]");
    }

    /**
     * Getter of new warnings.
     *
     * @return Number of new warnings
     */
    public int getNewWarningNumber() {
        return getIntByXPath("//table[@id='summary']/tbody/tr/td[@class='pane'][2]");
    }

    /**
     * Getter of fixed warnings.
     *
     * @return Number of fixed warnings
     */
    public int getFixedWarningNumber() {
        return getIntByXPath("//table[@id='summary']/tbody/tr/td[@class='pane'][3]");
    }

    /**
     * Getter of high warnings.
     *
     * @return Number of high warnings
     */
    public int getHighWarningNumber() {
        return getPriorityWarningNumber("HIGH");
    }

    /**
     * Getter of normal warnings.
     *
     * @return Number of normal warnings
     */
    public int getNormalWarningNumber() {
        return getPriorityWarningNumber("NORMAL");
    }

    /**
     * Getter of low warnings.
     *
     * @return Number of low warnings.
     */
    public int getLowWarningNumber() {
        return getPriorityWarningNumber("LOW");
    }

    /**
     * Getter for the href-value with the supplied text of a link.
     *
     * @param linkText Link text with href to get
     * @return Href-value of link with given text
     */
    public String getResultLinkByXPathText(final String linkText) {
        final String htmlElement = find(by.xpath(".//A[text() = '" + linkText + "']")).getAttribute("outerHTML");
        return StringUtils.substringBetween(htmlElement, "\"");
    }

    /**
     * Getter for warning counts of a certain priority
     *
     * @param priority the desired priority whose number of warnings shall be extracted (LOW, NORMAL, HIGH)
     * @return number of warnings of that priority
     */
    private int getPriorityWarningNumber(String priority) {
        return getIntByXPath("//table[@id='analysis.summary']/tbody/tr/td[@class='pane']/div[@id='" + priority + "']");
    }

    /**
     * Get number of path.
     *
     * @param xPath Path to go for number to return
     * @return The searched number
     */
    private int getIntByXPath(String xPath) {
        open();
        return asInt(find(by.xpath(xPath)));
    }

    /**
     * Returns the integer value of the webelement.
     *
     * @param e Webelement with number to get
     * @return Integer value of webelement as Integer object
     */
    protected Integer asInteger(WebElement e) {
        String trimmedText = e.getText().trim();
        // if no line number is given, sometimes a '-' is returned
        if ("-".equals(trimmedText)) {
            return 0;
        }
        return Integer.decode(trimmedText);
    }

    /**
     * Returns the integer value of the webelement.
     *
     * @param e Webelement with number to get
     * @return Integer value of webelement
     */
    protected int asInt(WebElement e) {
        return Integer.parseInt(e.getText().trim());
    }

    /**
     * Returns the trimmed content of the weblement
     *
     * @param webElement the Webelement whose content shall be trimmed
     * @return String the trimmed content
     */
    protected String asTrimmedString(final WebElement webElement) {
        return webElement.getText().trim();
    }

    /**
     * Returns a list of trimmed contents of a list of {@link org.openqa.selenium.WebElement}s.
     *
     * @param elems the list whose contents shall be trimmed
     * @return the trimmed strings as list
     */
    protected List<String> asTrimmedStringList(final List<WebElement> elems) {
        List<String> elemStrings = new ArrayList<>();
        for (WebElement we : elems) {
            elemStrings.add(asTrimmedString(we));
        }

        return elemStrings;
    }

    /**
     * Returns the first two columns of the "Files"-tab as key => value pairs, skipping the header and footer
     * rows.
     *
     * @return a map of the first two columns. (first column => second column)
     */
    public SortedMap<String, Integer> getFileTabContents() {
        openTab(Tab.FILES);
        return getContentsOfVisibleTable(true, true);
    }

    /**
     * Returns the first two columns of the "Packages"-tab as key => value pairs, skipping the header and footer
     * rows.
     *
     * @return a map of the first two columns. (first column => second column)
     */
    public SortedMap<String, Integer> getPackagesTabContents() {
        openTab(Tab.PACKAGES);
        return getContentsOfVisibleTable(true, true);
    }

    /**
     * Returns the first two columns of the "Modules"-tab as key => value pairs, skipping the header and footer
     * rows.
     *
     * @return a map of the first two columns. (first column => second column)
     */
    public SortedMap<String, Integer> getModulesTabContents() {
        openTab(Tab.MODULES);
        return getContentsOfVisibleTable(true, true);
    }

    /**
     * Returns the first two columns of the "Categories"-tab as key => value pairs, skipping the header and footer
     * rows.
     *
     * @return a map of the first two columns. (first column => second column)
     */
    public SortedMap<String, Integer> getCategoriesTabContents() {
        openTab(Tab.CATEGORIES);
        return getContentsOfVisibleTable(true, true);
    }

    /**
     * Returns the first two columns of the "Types"-tab as key => value pairs, skipping the header and footer rows.
     *
     * @return a map of the first two columns. (first column => second column)
     */
    public SortedMap<String, Integer> getTypesTabContents() {
        openTab(Tab.TYPES);
        return getContentsOfVisibleTable(true, true);
    }

    /**
     * Returns the first two columns of the "Warnings"-tab as key => value pairs, skipping the header row.
     *
     * @return a map of the first two columns. (first column => second column)
     */
    public SortedMap<String, Integer> getWarningsTabContents() {
        openTab(Tab.WARNINGS);
        return getContentsOfVisibleTable(true, false);
    }

    /**
     * Returns the first two columns of the "Fixed"-tab as key => value pairs, skipping the header row.
     *
     * @return a map of the first two columns. (first column => second column)
     */
    public SortedMap<String, String> getFixedTabContents() {
        openTab(Tab.FIXED);
        return getContentsOfVisibleTable(String.class, true, false);
    }

    private SortedMap<String, Integer> getContentsOfVisibleTable(boolean removeHeader, boolean removeFooter) {
        return mapTableCellsKeyValue(getVisibleTableRows(removeHeader, removeFooter));
    }

    /**
     * This is a generic variant of {@link AnalysisAction#getContentsOfVisibleTable(boolean, boolean)} which does not
     * care about the type of the value part as long as it is derived from {@link java.lang.Object}.
     */
    private <T extends Object> SortedMap<String, T> getContentsOfVisibleTable(Class<T> type, boolean removeHeader, boolean removeFooter) {
        return mapTableCellsKeyValue(type, getVisibleTableRows(removeHeader, removeFooter));
    }

    protected List<WebElement> getVisibleTableRows(boolean removeHeader, boolean removeFooter) {
        WebElement table = find(by.xpath("//div[@id='statistics']/div/div/table"));
        final List<WebElement> immediateChildRows = table.findElements(by.xpath("./tbody/tr"));

        if (removeHeader) {
            immediateChildRows.remove(0);
        }

        if (removeFooter) {
            immediateChildRows.remove(immediateChildRows.size() - 1);
        }

        return immediateChildRows;
    }

    private SortedMap<String, Integer> mapTableCellsKeyValue(final Collection<WebElement> rows) {
        return mapTableCellsKeyValue(Integer.class, rows);
    }

    /**
     * This is a generic variant of {@link AnalysisAction#mapTableCellsKeyValue(java.util.Collection)} which does not
     * care about the type of the value part.
     * <p/>
     * At the moment the only supported types are Integer and String. Calling this method for other types results in a
     * {@link java.lang.IllegalStateException}.
     */
    private <T> SortedMap<String, T> mapTableCellsKeyValue(Class<T> type, final Collection<WebElement> rows) {
        final SortedMap<String, T> result = new TreeMap<>();
        for (WebElement elem : rows) {
            final List<WebElement> cells = elem.findElements(by.xpath("./td"));
            final String key = asTrimmedString(cells.get(0));
            T value = null;
            if (type.isAssignableFrom(Integer.class)) {
                value = type.cast(asInteger(cells.get(1)));
            }
            else if (type.isAssignableFrom(String.class)) {
                value = type.cast(asTrimmedString(cells.get(1)));
            }
            else {
                throw new IllegalStateException("Parameter type (" +
                        type.getSimpleName() + ") is not supported");
            }

            result.put(key, value);
        }
        return result;
    }

    /**
     * This is method ensures to be on the correct tab of the build result page.
     *
     * @param id the id of the tab to select
     */
    protected void openTab(final Tab id) {
        open();
        find(by.xpath(".//A[@href]/em/div[@id='" + id.name().toLowerCase(Locale.ENGLISH) + "']")).click();
    }

    /**
     * Returns the affected source code as String which is linked by the specified tab table entry.
     *
     * @param tabId    the tab to use
     * @param fileName the name of the file containing the warning
     * @param line     the line number
     * @return the source code line as String.
     */
    public String getLinkedSourceFileText(final Tab tabId, final String fileName, final int line) {
        return asTrimmedString(getLinkedSourceFileLine(tabId, fileName, line));
    }

    /**
     * Returns the tooltip in the affected source code as String which is linked by the specified tab table entry.
     *
     * @param tabId    the tab to use
     * @param fileName the name of the file containing the warning
     * @param line     the line number
     * @return the source code line as String.
     */
    public String getLinkedSourceFileToolTip(final Tab tabId, final String fileName, final int line) {
        return getLinkedSourceFileLine(tabId, fileName, line).getAttribute("tooltip").trim();
    }

    /**
     * Returns the source code file line which is linked by the "Warnings"-tab table entries. The particular line is
     * found via the warning priority as it is used as title attribute's value for this div object.
     *
     * @param tabId    the tab to use
     * @param fileName the name of the file containing the warning
     * @param line     the line number
     * @return the {@link org.openqa.selenium.WebElement} containing the source code line
     */
    private WebElement getLinkedSourceFileLine(final Tab tabId, final String fileName, final int line) {
        openTab(tabId);
        selectWarning(fileName, line);
        return findSourceCodeLine(line);
    }

    private WebElement findSourceCodeLine(final int line) {
        return find(by.xpath("//div[@id='line" + line + "']"));
    }

    private void selectWarning(final String fileName, final int line) {
        find(by.xpath(".//A[text() = '" + fileName + ":" + line + "']")).click();
    }

    /**
     * This method gets line number (provided in the anchor) of the source code file which is linked by the
     * "Warnings"-tab table entries.
     *
     * @param tabId    the tab to use
     * @param fileName the name of the file containing the warning
     * @param line     the line number
     * @return the source code line as String.
     */
    public Integer getLinkedSourceFileLineNumber(final Tab tabId, final String fileName, final int line) {
        return asInt(getLinkedSourceFileLine(tabId, fileName, line).findElement(by.tagName("a")));
    }

    public enum Tab {
        MODULES, FILES, PACKAGES, WARNINGS, DETAILS, FIXED, NEW, CATEGORIES, TYPES
    }
}