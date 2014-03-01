package org.jenkinsci.test.acceptance.po;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Injector;
import groovy.lang.Closure;

import javax.inject.Inject;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;

import static org.hamcrest.CoreMatchers.*;
import static org.jenkinsci.test.acceptance.Matchers.*;

/**
 * Encapsulates a model in Jenkins and wraps interactions with it.
 *
 * See https://code.google.com/p/selenium/wiki/PageObjects
 *
 * <p>
 * Most non-trivial page objects should derive from {@link ContainerPageObject}.
 *
 * @author Kohsuke Kawaguchi
 */
@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
public abstract class PageObject extends CapybaraPortingLayer {
    @Inject
    protected ObjectMapper jsonParser;

    /**
     * Full URL of the object that this page object represents. Ends with '/',
     * like "http://localhsot:8080/job/foo/"
     *
     * @see #url(String)
     */
    public final URL url;

    private static final AtomicLong IOTA = new AtomicLong(System.currentTimeMillis());

    public PageObject(Injector injector, URL url) {
        super(injector);
        this.url = url;
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

    public String createRandomName() {
        return "rand_name_"+IOTA.incrementAndGet();
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
            throw new AssertionError("Failed to read from "+getJsonApiUrl(),e);
        }
    }

    /**
     * Visits the top page of this object.
     */
    public void open() {
        visit(url);
    }
}
