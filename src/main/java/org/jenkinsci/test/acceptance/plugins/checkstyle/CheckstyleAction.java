package org.jenkinsci.test.acceptance.plugins.checkstyle;

import org.jenkinsci.test.acceptance.po.ContainerPageObject;
import org.openqa.selenium.WebElement;

import java.net.URL;
import java.util.*;

/**
 * Page object for Checkstyle action.
 */
public class CheckstyleAction extends ContainerPageObject {
    private final ContainerPageObject parent;

    public CheckstyleAction(ContainerPageObject parent) {// Build or Job
        super(parent, parent.url("checkstyle/"));
        this.parent = parent;
    }

    public URL getHighPrioUrl() {
        return parent.url("checkstyleResult/HIGH");
    }

    public int getWarningNumber() {
        open();
        return asInt(find(by.xpath("//table[@id='summary']/tbody/tr/td[@class='pane'][1]")));
    }

    public int getNewWarningNumber() {
        open();
        return asInt(find(by.xpath("//table[@id='summary']/tbody/tr/td[@class='pane'][2]/a")));
    }

    public int getFixedWarningNumber() {
        open();
        return asInt(find(by.xpath("//table[@id='summary']/tbody/tr/td[@class='pane'][3]")));
    }

    public int getHighWarningNumber() {
        open();
        return asInt(find(by.xpath("//table[@id='analysis.summary']/tbody/tr/td[@class='pane'][2]/a")));
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

    public SortedMap<String, Integer> getFileNamesAndNumberOfWarnings() {
        open();
        final List<WebElement> rows = all(by.xpath("//table[@id='files']/tbody/tr"));

        // Remove footer and header
        rows.remove(0);
        rows.remove(rows.size() - 1);

        // map rows to "file => number warnings"
        return mapTableCellsKeyValue(rows);
    }

    public SortedMap<String, Integer> getCategoriesAndNumberOfWarnings() {
        open();
        find(by.xpath(".//A[@href]/em[text() = 'Categories']")).click();

        final List<WebElement> rows = all(by.xpath("(//table[@id='modules'])[1]/tbody/tr")); // a real unique identifier would help here

        // Remove footer and header
        rows.remove(0);
        rows.remove(rows.size() - 1);

        // map rows to "Category => number warnings"
        return mapTableCellsKeyValue(rows);
    }

    public SortedMap<String, Integer> getTypesAndNumberOfWarnings() {
        open();
        find(by.xpath(".//A[@href]/em[text() = 'Categories']")).click(); // intentional, the xpath-selector is not robust without this
        find(by.xpath(".//A[@href]/em[text() = 'Types']")).click();

        final List<WebElement> rows = all(by.xpath("(//table[@id='modules'])[2]/tbody/tr")); // a real unique identifier would help here too

        // Remove footer and header
        rows.remove(0);
        rows.remove(rows.size() - 1);

        // map rows to "Type => number warnings"
        return mapTableCellsKeyValue(rows);
    }

    private SortedMap<String, Integer> mapTableCellsKeyValue(final List<WebElement> rows) {
        final SortedMap<String, Integer> result = new TreeMap<String, Integer>();
        for(WebElement elem : rows) {
            final List<WebElement> cells = elem.findElements(by.xpath("./td"));
            final String key = asTrimmedString(cells.get(0));
            final Integer value = asInt(cells.get(1));
            result.put(key, value);
        }
        return result;
    }

    private String asTrimmedString(final WebElement webElement) {
        return webElement.getText().trim();
    }
}
