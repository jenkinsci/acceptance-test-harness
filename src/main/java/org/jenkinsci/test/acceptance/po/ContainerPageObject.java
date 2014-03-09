package org.jenkinsci.test.acceptance.po;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Injector;
import groovy.lang.Closure;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Callable;

import static org.hamcrest.CoreMatchers.*;
import static org.jenkinsci.test.acceptance.Matchers.*;

/**
 * {@link PageObject} that represents a model that has multiple views underneath.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class ContainerPageObject extends PageObject {
    protected ContainerPageObject(Injector injector, URL url) {
        super(injector, url);
        if (!url.toExternalForm().endsWith("/"))
            throw new IllegalArgumentException("URL should end with '/': "+url);
    }

    protected ContainerPageObject(PageObject context, URL url) {
        this(context.injector,url);
    }

    /**
     * Given the path relative to {@link #url}, visit that page
     */
    public void visit(String relativePath) {
        visit(url(relativePath));
    }

    /**
     * Resolves relative path against {@link #url} and treats any exception a a fatal problem.
     */
    public URL url(String rel) {
        try {
            return new URL(url,rel);
        } catch (MalformedURLException e) {
            throw new AssertionError(e);
        }
    }

    public URL url(String format, Object... args) {
        return url(String.format(format,args));
    }

    public void configure(Closure body) {
        configure();
        body.call(this);
        save();
    }

    public <T> T configure(Callable<T> body) {
        try {
            configure();
            T v = body.call();
            save();
            return v;
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    public void configure() {
        visit(getConfigUrl());
    }

    /**
     * Makes sure that the browser is currently opening the configuration page.
     */
    public void ensureConfigPage() {
        assertThat(driver.getCurrentUrl(), is(getConfigUrl().toExternalForm()));
    }

    public URL getConfigUrl() {
        return url("configure");
    }

    public void save() {
        clickButton("Save");
        assertThat(driver, not(hasContent("This page expects a form submission")));
    }

    public URL getJsonApiUrl() {
        return url("api/json");
    }

    /**
     * Makes the API call and obtains JSON representation.
     */
    public JsonNode getJson() {
        try {
            return jsonParser.readTree(getJsonApiUrl());
        } catch (IOException e) {
            throw new RuntimeException("Failed to read from "+getJsonApiUrl(),e);
        }
    }
}
