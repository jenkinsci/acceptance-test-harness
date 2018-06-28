package org.jenkinsci.test.acceptance.plugins.warnings.issues_table;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.jenkinsci.test.acceptance.plugins.warnings.source_code.SourceCodeView;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * Class representing a duplicate code warnings issue table row in the issues table.
 */
public class DRYIssuesTableRow extends AbstractNonDetailsIssuesTableRow {

    public static final String DUPLICATED_IN = "Duplicated In";
    public static final String AMOUNT_OF_LINES = "#Lines";
    private final String FILE_LINE_SEPARATOR = ":";

    /**
     * Creates an instance representing a duplicate code warnings table row.
     *
     * @param element
     *         the WebElement representing the row
     * @param issuesTable
     *         the issues table in which this row is displayed in
     */
    DRYIssuesTableRow(final WebElement element, final IssuesTable issuesTable) {
        super(element, issuesTable);
    }

    /**
     * Returns the value displayed in the #Lines column.
     *
     * @return the number of lines
     */
    public int getLines() {
        return Integer.parseInt(getCellContent(AMOUNT_OF_LINES));
    }

    /**
     * Returns the duplications as a list of Strings.
     *
     * @return the duplications
     */
    public List<String> getDuplicatedIn() {
        return getCells().get(getHeaders().indexOf(DUPLICATED_IN))
                .findElements(By.tagName("li"))
                .stream()
                .map(WebElement::getText)
                .collect(
                        Collectors.toList());
    }

    /**
     * Returns the file name in which the duplicate code was detected in
     *
     * @return the file name
     */
    public String getFile() {
        return getCellContent(FILE).split(FILE_LINE_SEPARATOR)[0];
    }

    /**
     * Returns the line number of the duplicated code warning.
     *
     * @return the line number
     */
    public int getLineNumber() {
        return Integer.parseInt(getCellContent(FILE).split(FILE_LINE_SEPARATOR)[1]);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null || obj.getClass() != getClass()) {
            return false;
        }
        DRYIssuesTableRow that = (DRYIssuesTableRow) obj;
        return Objects.equals(that.getFile(), this.getFile()) && Objects.equals(that.getLineNumber(),
                this.getLineNumber()) && Objects.equals(that.getLines(), this.getLines());
    }

    /**
     * Performs a click on the link to the source code page.
     *
     * @return the representation of the source code page.
     */
    public SourceCodeView clickOnFileLink() {
        return clickOnLink(getCell(FILE).findElement(By.tagName("a")), SourceCodeView.class);
    }

    /**
     * Performs a click on a specific link of the detected duplications.
     *
     * @param number
     *         the number of the link which shall be clicked
     *
     * @return the representation of the source code page.
     */
    public SourceCodeView clickOnDuplicatedInLink(int number) {
        return clickOnLink(findAllLinks(getCell(DUPLICATED_IN)).get(number), SourceCodeView.class);
    }
}
