package plugins.warnings.assertions;

import java.util.Objects;

import org.assertj.core.api.AbstractAssert;
import org.jenkinsci.test.acceptance.plugins.warnings_ng.ConsoleLogView;

/**
 * Assertions for {@link ConsoleLogView}.
 *
 * @author Ullrich Hafner
 */
@SuppressWarnings({"ParameterHidesMemberVariable", "NonBooleanMethodNameMayNotStartWithQuestion"})
public class ConsoleLogViewAssert extends AbstractAssert<ConsoleLogViewAssert, ConsoleLogView> {
    private static final String EXPECTED_BUT_WAS_MESSAGE = "%nExpecting %s of:%n <%s>%nto be:%n <%s>%nbut was:%n <%s>.";
    
    /**
     * Creates a new {@link ConsoleLogViewAssert} to make assertions on actual {@link ConsoleLogView}.
     *
     * @param actual
     *         the view we want to make assertions on
     */
    ConsoleLogViewAssert(final ConsoleLogView actual) {
        super(actual, ConsoleLogViewAssert.class);
    }

    /**
     * An entry point for {@link ConsoleLogViewAssert} to follow AssertJ standard {@code assertThat()}. With a static import,
     * one can write directly {@code assertThat(view)} and get a specific assertion with code completion.
     *
     * @param actual
     *         the view we want to make assertions on
     *
     * @return a new {@link ConsoleLogViewAssert}
     */
    public static ConsoleLogViewAssert assertThat(final ConsoleLogView actual) {
        return new ConsoleLogViewAssert(actual);
    }

    /**
     * Verifies that the title of the console log view is equal to the expected one.
     *
     * @param expectedTitle
     *         the expected title
     *
     * @return this assertion object
     * @throws AssertionError
     *         if the actual title is not equal to the given one
     */
    public ConsoleLogViewAssert hasTitle(final String expectedTitle) {
        isNotNull();

        if (!Objects.equals(actual.getTitle(), expectedTitle)) {
            failWithMessage(EXPECTED_BUT_WAS_MESSAGE, "title", actual, expectedTitle, actual.getTitle());
        }
        return this;
    }

    /**
     * Verifies that the highlighted text of the console log view is equal to the expected one.
     *
     * @param expectedHighlightedText
     *         the expected highlighted text
     *
     * @return this assertion object
     * @throws AssertionError
     *         if the actual highlighted text is not equal to the given one
     */
    public ConsoleLogViewAssert hasHighlightedText(final String expectedHighlightedText) {
        isNotNull();

        if (!Objects.equals(actual.getHighlightedText(), expectedHighlightedText)) {
            failWithMessage(EXPECTED_BUT_WAS_MESSAGE, "highlightedText", actual, expectedHighlightedText, actual.getHighlightedText());
        }
        return this;
    }
}
