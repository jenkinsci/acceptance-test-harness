package org.jenkinsci.test.acceptance.po;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Injector;
import groovy.lang.Closure;
import org.openqa.selenium.TimeoutException;

import javax.inject.Inject;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.hamcrest.CoreMatchers.*;

/**
 * @author Kohsuke Kawaguchi
 */
@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
public abstract class PageObject extends CapybaraPortingLayer {
    @Inject
    protected ObjectMapper jsonParser;

    /**
     * Full URL of the object that this page object represents. Ends with '/',
     * like "http://localhsot:8080/job/foo/"
     */
    public final URL url;

    private static final AtomicLong IOTA = new AtomicLong(System.currentTimeMillis());

    public PageObject(Injector injector, URL url) {
        super(injector);
        this.url = url;
        if (!url.toExternalForm().endsWith("/"))
            throw new IllegalArgumentException("URL should end with '/': "+url);
    }

    /**
     * Given the path relative to {@link #url}, visit that page
     */
    public void visit(String relativePath) throws Exception {
        visit(new URL(url,relativePath));
    }

    public String createRandomName() {
        return "rand_name_"+IOTA.incrementAndGet();
    }

    public void configure(Closure body) throws Exception {
        configure();
        body.call(this);
        save();
    }

    public <T> T configure(Callable<T> body) throws Exception {
        configure();
        T v = body.call();
        save();
        return v;
    }

    public void configure() throws Exception {
        visit(getConfigUrl());
    }

    /**
     * Makes sure that the browser is currently opening the configuration page.
     */
    public void ensureConfigPage() throws Exception {
        assertThat(driver.getCurrentUrl(), is(getConfigUrl().toExternalForm()));
    }

    public URL getConfigUrl() throws Exception {
        return new URL(url,"configure");
    }

    public void save() {
        clickButton("Save");
        if (driver.getPageSource().contains("This page expects a form submission")) {
            throw new AssertionError("Job was not saved\n"+driver.getPageSource());
        }
    }

    public URL getJsonApiUrl() throws Exception {
        return new URL(url,"api/json");
    }

    /**
     * Makes the API call and obtains JSON representation.
     */
    public JsonNode getJson() throws Exception {
        return jsonParser.readTree(getJsonApiUrl());
    }

    /**
     * Repeated evaluate the given predicate until it returns true.
     *
     * If it times out, an exception will be thrown.
     */
    public void waitForCond(Callable<Boolean> block, int timeoutSec) throws Exception {
        long endTime = System.currentTimeMillis()+ TimeUnit.SECONDS.toMillis(timeoutSec);
        while (System.currentTimeMillis()<endTime) {
            if (block.call())
                return;
            Thread.sleep(1000);
        }
        throw new TimeoutException("Failed to wait for condition "+block);
    }

    public void waitForCond(Callable<Boolean> block) throws Exception {
        waitForCond(block,30);
    }

    /**
     * Visits the top page of this object.
     */
    public void open() throws Exception {
        visit(".");
    }
}
