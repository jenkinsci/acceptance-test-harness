package org.jenkinsci.test.acceptance.plugins.dashboard_view.controls;

import java.util.Arrays;
import java.util.List;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.PageAreaImpl;
import org.jenkinsci.test.acceptance.po.PageObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * Provides the control area for setting the Columns to display in the default job list.
 *
 * @author Peter Müller
 */
public class ColumnsArea extends PageAreaImpl {
    /**
     * Resembles the select for adding a new column to the list.
     */
    private final Control addColumns = control("/hetero-list-add[columns]");

    /**
     * Creates a new PageArea for the config list of columns that should be shown in the default job list.
     */
    public ColumnsArea(PageObject context, String path) {
        super(context, path);
    }

    /**
     * Get the delete button for the list item of the column.
     *
     * @param column column, the name of column in the list item.
     * @return A control for the delete button
     */
    private Control getDeleteFor(Column column) {
        final By xpath =
                By.xpath("//div[@name='columns' and contains(.,'" + column.getText() + "')]//button[@title='Delete']");
        return control(xpath);
    }

    /**
     * Remove the list item for a column from the list of displayed columns.
     *
     * @param column column, the name of the column that will be removed.
     */
    public void remove(Column column) {
        final Control deleteButton = getDeleteFor(column);
        deleteButton.click();
    }

    /**
     * Removes all Columns as present in the default configuration.
     */
    public void removeAll() {
        By form = By.xpath("//form[@name='viewConfig']");
        By xpath = By.xpath("//div[@name='columns' and not(contains(.,'" + Column.LAST_STABLE.getText()
                + "'))]//button[@title='Delete']");
        List<WebElement> columns = control(form).resolve().findElements(xpath);
        Arrays.stream(columns.toArray(new WebElement[0])).forEach(WebElement::click);
    }

    /**
     * Add a column item by name to the list.
     *
     * @param column column, the column to add.
     */
    public void add(Column column) {
        addColumns.selectDropdownMenu(column.getText());
    }

    /**
     * Provides a simple enumeration for all possible Columns.
     */
    public enum Column {
        STATUS("Status"),
        WEATHER("Weather"),
        NAME("Name"),
        LAST_SUCCESS("Last Success"),
        LAST_FAILURE("Last Failure"),
        LAST_STABLE("Last Stable"),
        LAST_DURATION("Last Duration"),
        BUILD_BUTTON("Build Button");

        private final String text;

        Column(String text) {
            this.text = text;
        }

        /**
         * Get the text value how it will be displayed in the browser.
         *
         * @return the text representation of the column
         */
        public String getText() {
            return text;
        }
    }
}
