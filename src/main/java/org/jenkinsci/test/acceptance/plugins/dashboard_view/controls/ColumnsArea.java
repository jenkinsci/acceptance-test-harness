package org.jenkinsci.test.acceptance.plugins.dashboard_view.controls;

import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.PageAreaImpl;
import org.jenkinsci.test.acceptance.po.PageObject;
import org.openqa.selenium.By;

import java.util.Arrays;

/**
 * Created by peter-mueller on 27.04.17.
 */
public class ColumnsArea extends PageAreaImpl {
    private final Control addColumns = control("/hetero-list-add[columns]");


    public ColumnsArea(PageObject context, String path) {
        super(context, path);
    }

    private Control getDeleteFor(Column column) {
        return control(By.xpath("//div[@name='columns' and contains(.,'" +
                column.getText() + "')]//button[@title='Delete']"));
    }

    public void remove(Column column) {
        final Control deleteButton = getDeleteFor(column);
        deleteButton.click();
    }

    public void removeAll() {
        Arrays.stream(Column.values())
                .filter(c -> !Column.LAST_STABLE.equals(c))
                .forEach(this::remove);
    }

    public void add(Column column) {
        addColumns.selectDropdownMenu(column.getText());
    }

    public static enum Column {
        STATUS("Status"), WEATHER("Weather"), NAME("Name"), LAST_SUCCESS("Last Success"),
        LAST_FAILURE("Last Failure"), LAST_STABLE("Last Stable"), LAST_DURATION("Last Duration"),
        BUILD_BUTTON("Build Button");

        private final String text;

        Column(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }
    }
}
