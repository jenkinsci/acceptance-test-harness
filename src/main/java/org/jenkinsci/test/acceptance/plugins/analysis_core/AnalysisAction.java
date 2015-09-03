package org.jenkinsci.test.acceptance.plugins.analysis_core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.ContainerPageObject;
import org.jenkinsci.test.acceptance.po.Job;
import org.openqa.selenium.WebElement;

/**
 * Abstract action class for static analysis plugins with getters for warnings.
 *
 * @author Martin Kurz
 */
public abstract class AnalysisAction extends ContainerPageObject {
    private final ContainerPageObject parent;
    private final String plugin;

    /**
     * Creates a new instance of a static analysis action.
     *
     * @param parent Parent container page object
     * @param plugin plug-in name
     */
    public AnalysisAction(final Build parent, final String plugin) {
        this(plugin + "Result", parent);
    }

    /**
     * Creates a new instance of a static analysis action.
     *
     * @param parent Parent container page object
     * @param plugin Path to plugin without / at the end
     */
    public AnalysisAction(final Job parent, final String plugin) {
        this(plugin, parent);
    }

    private AnalysisAction(String url, ContainerPageObject parent) {
        super(parent, parent.url(url + '/'));

        this.parent = parent;
        plugin = url;
    }

    /**
     * Returns the URL of this action.
     *
     * @return  URL of this action
     */
    public String getUrl() {
        return plugin;
    }

    /**
     * Returns the number of warnings.
     *
     * @return number of warnings
     */
    public int getNumberOfWarnings() {
        return getSummaryCellXPath("all");
    }

    /**
     * Returns the number of new warnings.
     *
     * @return number of new warnings
     */
    public int getNumberOfNewWarnings() {
        return getSummaryCellXPath("new");
    }

    /**
     * Returns the number of fixed warnings.
     *
     * @return number of fixed warnings
     */
    public int getNumberOfFixedWarnings() {
        return getSummaryCellXPath("fixed");
    }

    private int getSummaryCellXPath(final String element) {
        return getIntByXPath("//td[@id='" + element + "']");
    }

    /**
     * Returns the number of warnings with high priority.
     *
     * @return number of warnings with high priority
     */
    public int getNumberOfWarningsWithHighPriority() {
        return getPriorityWarningNumber("HIGH");
    }

    /**
     * Returns the number of warnings with normal priority.
     *
     * @return number of warnings with normal priority
     */
    public int getNumberOfWarningsWithNormalPriority() {
        return getPriorityWarningNumber("NORMAL");
    }

    /**
     * Returns the number of warnings with low priority.
     *
     * @return number of warnings with low priority
     */
    public int getNumberOfWarningsWithLowPriority() {
        return getPriorityWarningNumber("LOW");
    }

    private int getPriorityWarningNumber(final String priority) {
        return getIntByXPath("//div[@id='" + priority + "']");
    }

    /**
     * Getter for the href-value with the supplied text of a link.
     *
     * @param linkText Link text with href to get
     * @return Href-value of link with given text
     */
    public String getResultLinkByXPathText(final String linkText) {
        String htmlElement = find(by.xpath(".//A[text() = '" + linkText + "']")).getAttribute("outerHTML");
        return StringUtils.substringBetween(htmlElement, "\"");
    }

    /**
     * Get number of path.
     *
     * @param xPath Path to go for number to return
     * @return The searched number
     */
    private int getIntByXPath(String xPath) {
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
     * Returns the number of fixed warnings by counting the number of rows in the 'fixed' warnings table.
     *
     * @return the number of fixed warnings
     */
    // TODO: actually the content of each row should be validated
    public int getNumberOfRowsInFixedWarningsTable() {
        return getVisibleTableRows(true, false, find(by.xpath("//table[@id='fixed']"))).size();
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
        return getVisibleTableRows(removeHeader, removeFooter, table);
    }

    private List<WebElement> getVisibleTableRows(final boolean removeHeader, final boolean removeFooter, final WebElement table) {
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
     * Opens the details page that show the new warnings.
     */
    public void openNew() {
        visit("new");
    }

    /**
     * Opens the details page that show the fixed warnings.
     */
    public void openFixed() {
        visit("fixed");
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

    public String getAnnotationName() {
        return "warning";
    }

    public enum Tab {
        MODULES, FILES, PACKAGES, WARNINGS, DETAILS, FIXED, NEW, CATEGORIES, TYPES
    }
}