package org.jenkinsci.test.acceptance.po;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.jenkinsci.test.acceptance.Matchers.hasContent;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.io.IOException;
import java.net.HttpURLConnection;
import org.jenkinsci.test.acceptance.utils.IOUtil;

/**
 * Artifact of a build
 *
 * @author Kohsuke Kawaguchi
 */
public class Artifact extends PageObject {
    public final @NonNull Build build;
    private final @NonNull String path;

    public Artifact(@NonNull Build build, @NonNull String path) {
        super(build, build.url("artifact/%s", path));
        this.build = build;
        this.path = path;
    }

    public String getRelativePath() {
        return path;
    }

    /**
     * Asserts that this artifact have the given content.
     */
    public void shouldHaveContent(String content) {
        visit(url("%s/*view*/", path));
        assertThat(driver, hasContent(content));
    }

    public String getTextContent() {
        visit(url("%s/*view*/", path));
        return getPageContent(driver);
    }

    public void assertThatExists(Boolean should) {
        try {
            HttpURLConnection con = IOUtil.openConnection(url);
            assertThat(con.getResponseCode(), is(should ? 200 : 404));
        } catch (IOException e) {
            throw new AssertionError("Failed to check status of " + url, e);
        }
    }
}
