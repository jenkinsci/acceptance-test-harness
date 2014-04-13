package org.jenkinsci.test.acceptance.po;

import org.jenkinsci.test.acceptance.cucumber.Should;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.hamcrest.CoreMatchers.*;
import static org.jenkinsci.test.acceptance.Matchers.*;

/**
 * Artifact of a build
 *
 * @author Kohsuke Kawaguchi
 */
public class Artifact extends PageObject {
    public final Build build;

    public Artifact(Build build, URL url) {
        super(build.injector,url);
        this.build = build;
    }

    /**
     * Asserts that this artifact have the given content.
     */
    public void shouldHaveContent(String content) {
        open();
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
            throw new AssertionError("Failed to check status of "+url,e);
        }
    }
}
