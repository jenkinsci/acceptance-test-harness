package org.jenkinsci.test.acceptance.plugins.warnings.white_mountains;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * Class representing a duplicate code warnings issue table row in the issues table.
 */
public class DryIssuesTableRow extends AbstractNonDetailsIssuesTableRow {
    private static final String DUPLICATED_IN = "Duplicated In";
    private static final String AMOUNT_OF_LINES = "#Lines";

    /**
     * Creates an instance representing a duplicate code warnings table row.
     *
     * @param element
     *         the WebElement representing the row
     * @param issuesTable
     *         the issues table in which this row is displayed in
     */
    DryIssuesTableRow(final WebElement element, final IssuesTable issuesTable) {
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
                .collect(Collectors.toList());
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null || obj.getClass() != getClass()) {
            return false;
        }
        DryIssuesTableRow that = (DryIssuesTableRow) obj;
        return Objects.equals(that.getFileName(), this.getFileName()) && Objects.equals(that.getLineNumber(),
                this.getLineNumber()) && Objects.equals(that.getLines(), this.getLines());
    }

    /**
     * Performs a click on the link to the source code page.
     *
     * @return the representation of the source code page.
     */
    public SourceView clickOnFileLink() {
        return clickOnLink(getFileLink(), SourceView.class);
    }

    /**
     * Performs a click on a specific link of the detected duplications.
     *
     * @param number
     *         the number of the link which shall be clicked
     *
     * @return the representation of the source code page.
     */
    public SourceView clickOnDuplicatedInLink(final int number) {
        return clickOnLink(findAllLinks(getCell(DUPLICATED_IN)).get(number), SourceView.class);
    }
}
