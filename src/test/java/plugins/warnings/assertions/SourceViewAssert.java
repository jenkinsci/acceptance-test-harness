package plugins.warnings.assertions;

import java.util.Objects;

import org.assertj.core.api.AbstractAssert;
import org.jenkinsci.test.acceptance.plugins.warnings.white_mountains.SourceView;

/**
 * Assertions for {@link SourceView}.
 *
 * @author Ullrich Hafner
 */
@SuppressWarnings({"ParameterHidesMemberVariable", "NonBooleanMethodNameMayNotStartWithQuestion"})
public class SourceViewAssert extends AbstractAssert<SourceViewAssert, SourceView> {
    private static final String EXPECTED_BUT_WAS_MESSAGE = "%nExpecting %s of:%n <%s>%nto be:%n <%s>%nbut was:%n <%s>.";
    
    /**
     * Creates a new {@link SourceViewAssert} to make assertions on actual {@link SourceView}.
     *
     * @param actual
     *         the view we want to make assertions on
     */
    SourceViewAssert(final SourceView actual) {
        super(actual, SourceViewAssert.class);
    }

    /**
     * An entry point for {@link SourceViewAssert} to follow AssertJ standard {@code assertThat()}. With a static import,
     * one can write directly {@code assertThat(view)} and get a specific assertion with code completion.
     *
     * @param actual
     *         the view we want to make assertions on
     *
     * @return a new {@link SourceViewAssert}
     */
    public static SourceViewAssert assertThat(final SourceView actual) {
        return new SourceViewAssert(actual);
    }

    /**
     * Verifies that the file name of the source code view is equal to the expected one.
     *
     * @param expectedFileName
     *         the expected file name
     *
     * @return this assertion object
     * @throws AssertionError
     *         if the actual file name is not equal to the given one
     */
    public SourceViewAssert hasFileName(final String expectedFileName) {
        isNotNull();

        if (!Objects.equals(actual.getFileName(), expectedFileName)) {
            failWithMessage(EXPECTED_BUT_WAS_MESSAGE, "fileName", actual, expectedFileName, actual.getFileName());
        }
        return this;
    }

    /**
     * Verifies that the source code of the source code view is equal to the expected one.
     *
     * @param expectedSourceCode
     *         the expected source code
     *
     * @return this assertion object
     * @throws AssertionError
     *         if the actual source code is not equal to the given one
     */
    public SourceViewAssert hasSourceCode(final String expectedSourceCode) {
        isNotNull();

        if (!Objects.equals(strip(actual.getSourceCode()), strip(expectedSourceCode))) {
            failWithMessage(EXPECTED_BUT_WAS_MESSAGE, "sourceCode", actual, expectedSourceCode, actual.getSourceCode());
        }
        return this;
    }

    private String strip(final String value) {
        return value.replaceAll("\\s*", "");
    }
}
