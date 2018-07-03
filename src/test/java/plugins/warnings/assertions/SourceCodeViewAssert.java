package plugins.warnings.assertions;

import java.util.List;

import org.assertj.core.api.AbstractAssert;
import org.jenkinsci.test.acceptance.plugins.warnings.SourceCodeView;

/**
 * A simple custom assertion for the SourceCodeView.
 *
 * @author Frank Christian Geyer
 * @author Deniz Mardin
 */
public class SourceCodeViewAssert extends AbstractAssert<SourceCodeViewAssert, SourceCodeView> {

    SourceCodeViewAssert(final SourceCodeView actual) {
        super(actual, SourceCodeViewAssert.class);
    }

    /**
     * A simple constructor.
     *
     * @param actual
     *         the actual SourceCodeView object
     *
     * @return the SourceCodeViewAssert object
     */
    public static SourceCodeViewAssert assertThat(final SourceCodeView actual) {
        return new SourceCodeViewAssert(actual);
    }

    /**
     * Verifies that the sources are correct.
     *
     * @return the SourceCodeViewAssert
     */
    public SourceCodeViewAssert hasCorrectSources(final List<String> expectedSources) {
        isNotNull();

        for (int i = 0; i < expectedSources.size(); i++) {
            if (!expectedSources.get(i).equals(actual.getFileContentList().get(i))) {
                failWithMessage("Expected source to be %s but was %s",
                        expectedSources.get(i), actual.getFileContentList().get(i));
            }
        }
        return this;
    }

    /**
     * Verifies that the headers are correct.
     *
     * @param headers
     *         the headers
     *
     * @return the SourceCodeViewAssert
     */
    public SourceCodeViewAssert hasCorrectHeaders(final List<String> headers) {
        isNotNull();

        for (int i = 0; i < headers.size(); i++) {
            if (!headers.get(i).equals(actual.getHeaders().get(i))) {
                failWithMessage("Expected header to be %s but was %s",
                        headers.get(i), actual.getHeaders().get(i));
            }
        }
        return this;
    }

    /**
     * Verifies that the header size is correct.
     *
     * @param headerSize
     *         the size of the headers
     *
     * @return the SourceCodeViewAssert
     */
    public SourceCodeViewAssert hasCorrectHeaderSize(final int headerSize) {
        isNotNull();

        if (headerSize != actual.getHeaders().size()) {
            failWithMessage("Expected header size to be %s but was %s",
                    headerSize, actual.getHeaders().size());
        }
        return this;
    }

    /**
     * Verifies that the file number is correct.
     *
     * @param expectedFileSize
     *         the file size
     *
     * @return the SourceCodeViewAssert
     */
    public SourceCodeViewAssert hasCorrectFileSize(final int expectedFileSize) {
        isNotNull();

        if (actual.getFileContentList().size() != expectedFileSize) {
            failWithMessage("Expected %s files but there were %s files",
                    expectedFileSize, actual.getFileContentList().size());
        }
        return this;
    }

    /**
     * Verifies that there are exactly as many headers as files obtained.
     *
     * @return the SourceCodeViewAssert
     */
    public SourceCodeViewAssert fileSizeIsMatchingHeaderSize() {
        isNotNull();

        if (actual.getFileContentList().size() != actual.getHeaders().size()) {
            failWithMessage(
                    "Expected warning list to be equal to the header list regarding size. But warning list size was %s and header list size was %s",
                    actual.getFileContentList().size(), actual.getHeaders().size());
        }
        return this;
    }
}
