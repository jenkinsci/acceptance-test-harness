package org.jenkinsci.test.acceptance.po;

import org.jenkinsci.test.acceptance.cucumber.Should;

import java.io.IOException;
import java.net.HttpURLConnection;

import javax.annotation.Nonnull;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.jenkinsci.test.acceptance.Matchers.hasContent;

/**
 * Artifact of a build
 *
 * @author Kohsuke Kawaguchi
 */
public class Artifact extends PageObject {
    public final @Nonnull Build build;
    private final @Nonnull String path;

    public Artifact(@Nonnull Build build, @Nonnull String path) {
        super(build.injector, build.url("artifact/%s", path));
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

    /**
     * Asserts that this artifact should or shouldn't exist.
     */
    public void assertThatExists(Should should) {
        assertThatExists(should.value);
    }

    public void assertThatExists(Boolean should) {
        try {
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            assertThat(con.getResponseCode(), is(should ? 200 : 404));
        } catch (IOException e) {
            throw new AssertionError("Failed to check status of " + url, e);
        }
    }
}
