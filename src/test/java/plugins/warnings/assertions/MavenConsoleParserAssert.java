package plugins.warnings.assertions;

import java.util.Arrays;
import java.util.List;

import org.assertj.core.api.AbstractAssert;
import org.jenkinsci.test.acceptance.plugins.warnings.MavenConsoleParser;

/**
 * A simple custom assertion for the MavenConsoleParser.
 *
 * @author Frank Christian Geyer
 * @author Deniz Mardin
 */
public class MavenConsoleParserAssert extends AbstractAssert<MavenConsoleParserAssert, MavenConsoleParser> {

    private MavenConsoleParserAssert(final MavenConsoleParser actual) {
        super(actual, MavenConsoleParserAssert.class);
    }

    /**
     * A simple constructor.
     *
     * @param actual
     *         the actual MavenConsoleParser object
     *
     * @return the MavenConsoleParserAssert object
     */
    public static MavenConsoleParserAssert assertThat(final MavenConsoleParser actual) {
        return new MavenConsoleParserAssert(actual);
    }

    /**
     * Verifies that the expected strings are contained in the output of the web page.
     *
     * @param expectedMessages
     *         the expected messages
     *
     * @return the MavenConsoleParserAssert
     */
    public MavenConsoleParserAssert containsMessage(final List<String> expectedMessages) {
        isNotNull();

        for (int i = 0; i < expectedMessages.size(); i++) {
            if (!actual.getWarningContentList().contains(expectedMessages.get(i))) {
                failWithMessage(
                        "Expected message %s to be contained in crawled messages from the website but was not, content was: %s",
                        expectedMessages.get(i), Arrays.toString(actual.getWarningContentList().toArray())
                );
            }
        }
        return this;
    }

    /**
     * Verifies that the headers of the output are correct.
     *
     * @param expectedHeader
     *         the expected header
     *
     * @return the MavenConsoleParserAssert
     */
    public MavenConsoleParserAssert hasCorrectHeader(final String expectedHeader) {
        isNotNull();

        for (String headerFromWebPage : actual.getHeaders()) {
            if (!expectedHeader.equals(headerFromWebPage)) {
                failWithMessage("Expected header to be %s but was %s",
                        expectedHeader, headerFromWebPage);
            }
        }
        return this;
    }

    /**
     * Verifies that the there are exactly as many headers as files obtained.
     *
     * @param expectedMessageSize
     *         the expected message size
     *
     * @return the MavenConsoleParserAssert
     */
    public MavenConsoleParserAssert hasCorrectFileSize(final int expectedMessageSize) {
        isNotNull();

        if (actual.getWarningContentList().size() != expectedMessageSize) {
            failWithMessage("Expected message size to be %s but was %s",
                    expectedMessageSize, actual.getWarningContentList().size());
        }
        return this;
    }

    /**
     * Verifies that there are exactly as many headers as files obtained.
     *
     * @return the MavenConsoleParserAssert
     */
    public MavenConsoleParserAssert fileSizeIsMatchingHeaderSize() {
        isNotNull();

        if (actual.getWarningContentList().size() != actual.getHeaders().size()) {
            failWithMessage(
                    "Expected warning list to be equal to the header list regarding size. But warning list size was %s and header list size was %s",
                    actual.getWarningContentList().size(), actual.getHeaders().size());
        }
        return this;
    }
}
