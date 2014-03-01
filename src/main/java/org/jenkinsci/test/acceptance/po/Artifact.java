package org.jenkinsci.test.acceptance.po;

import java.net.URL;

import static org.jenkinsci.test.acceptance.Matchers.*;

/**
 * Artifact of a build
 *
 * @author Kohsuke Kawaguchi
 */
public class Artifact extends CapybaraPortingLayer {
    public final Build build;
    public final URL url;

    public Artifact(Build build, URL url) {
        super(build.injector);
        this.url = url;
        this.build = build;
    }

    /**
     * Asserts that this artifact have the given content.
     */
    public void shouldHaveContent(String content) throws Exception {
        visit(url);
        assertThat(driver, hasContent(content));
    }
}
